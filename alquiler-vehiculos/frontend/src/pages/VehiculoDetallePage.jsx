import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import StatusBadge from '../components/StatusBadge.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import {
  obtenerVehiculo,
  parseApiError,
} from '../services/vehiculosService.js'
import {
  crearSolicitud,
  parseApiError as parseOperacionesError,
} from '../services/operacionesService.js'
import styles from './VehiculoDetallePage.module.css'

function formatPrecio(precio) {
  if (precio == null) return '—'
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(Number(precio))
}

export default function VehiculoDetallePage() {
  const { id } = useParams()
  const [vehiculo, setVehiculo] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [fechaInicio, setFechaInicio] = useState('')
  const [fechaFin, setFechaFin] = useState('')
  const [formError, setFormError] = useState(null)
  const [formSuccess, setFormSuccess] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    const controller = new AbortController()

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const data = await obtenerVehiculo(id, { signal: controller.signal })
        setVehiculo(data)
      } catch (err) {
        if (controller.signal.aborted) return
        setError(parseApiError(err))
      } finally {
        if (!controller.signal.aborted) setLoading(false)
      }
    }

    load()
    return () => controller.abort()
  }, [id])

  async function handleSolicitudSubmit(event) {
    event.preventDefault()
    setFormError(null)
    setFormSuccess(null)

    if (!fechaInicio || !fechaFin) {
      setFormError('Indica fecha de inicio y fin del alquiler.')
      return
    }
    if (fechaFin < fechaInicio) {
      setFormError('La fecha de fin debe ser posterior o igual a la de inicio.')
      return
    }

    setSubmitting(true)
    try {
      await crearSolicitud({
        vehiculoId: Number(id),
        fechaInicio,
        fechaFin,
      })
      setFormSuccess('Solicitud registrada correctamente. Revisa la sección Operaciones.')
      setFechaInicio('')
      setFechaFin('')
    } catch (err) {
      setFormError(parseOperacionesError(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return <LoadingSpinner label="Cargando vehículo..." />
  }

  if (error) {
    return (
      <main className="page-container">
        <ErrorMessage message={error} />
        <Link to="/vehiculos">Volver al catálogo</Link>
      </main>
    )
  }

  const puedeSolicitar = vehiculo.estado === 'DISPONIBLE'

  return (
    <main className="page-container">
      <Link to="/vehiculos" className={styles.back}>
        ← Volver al catálogo
      </Link>

      <section className={styles.detail}>
        <header className={styles.header}>
          <h1>
            {vehiculo.marca} {vehiculo.modelo}
          </h1>
          <StatusBadge estado={vehiculo.estado} />
        </header>

        <dl className={styles.info}>
          <div>
            <dt>Matrícula</dt>
            <dd>{vehiculo.matricula}</dd>
          </div>
          <div>
            <dt>Precio por día</dt>
            <dd>{formatPrecio(vehiculo.precioPorDia)}</dd>
          </div>
          <div>
            <dt>Identificador</dt>
            <dd>{vehiculo.id}</dd>
          </div>
        </dl>
      </section>

      <section className={styles.solicitud}>
        <h2>Solicitar alquiler</h2>
        {!puedeSolicitar ? (
          <p className={styles.aviso}>
            Solo se pueden registrar solicitudes para vehículos en estado
            DISPONIBLE.
          </p>
        ) : (
          <form onSubmit={handleSolicitudSubmit} className={styles.form}>
            {formSuccess && (
              <div className="feedback-banner feedback-banner--success" role="status">
                {formSuccess}
              </div>
            )}
            {formError && (
              <div className="feedback-banner feedback-banner--error" role="alert">
                {formError}
              </div>
            )}
            <div className={styles.field}>
              <label htmlFor="fechaInicio">Fecha inicio</label>
              <input
                id="fechaInicio"
                type="date"
                value={fechaInicio}
                onChange={(e) => setFechaInicio(e.target.value)}
                disabled={submitting}
                required
              />
            </div>
            <div className={styles.field}>
              <label htmlFor="fechaFin">Fecha fin</label>
              <input
                id="fechaFin"
                type="date"
                value={fechaFin}
                onChange={(e) => setFechaFin(e.target.value)}
                disabled={submitting}
                required
              />
            </div>
            <button type="submit" disabled={submitting}>
              {submitting ? 'Enviando...' : 'Registrar solicitud'}
            </button>
          </form>
        )}
      </section>
    </main>
  )
}
