import { useState } from 'react'
import SolicitudCard from '../components/SolicitudCard.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useSolicitudes } from '../hooks/useSolicitudes.js'
import {
  confirmarSolicitud,
  cancelarSolicitud,
  parseApiError,
} from '../services/operacionesService.js'
import { ESTADOS_SOLICITUD } from '../constants/enums.js'
import styles from './OperacionesPage.module.css'

export default function OperacionesPage() {
  const [estadoFiltro, setEstadoFiltro] = useState('')
  const { data, loading, error, refetch } = useSolicitudes({
    estado: estadoFiltro,
  })
  const [actionLoadingId, setActionLoadingId] = useState(null)
  const [cardErrors, setCardErrors] = useState({})

  async function handleConfirmar(solicitud) {
    setActionLoadingId(solicitud.id)
    setCardErrors((prev) => ({ ...prev, [solicitud.id]: null }))
    try {
      await confirmarSolicitud(solicitud.id)
      refetch()
    } catch (err) {
      setCardErrors((prev) => ({
        ...prev,
        [solicitud.id]: parseApiError(err),
      }))
    } finally {
      setActionLoadingId(null)
    }
  }

  async function handleCancelar(solicitud) {
    setActionLoadingId(solicitud.id)
    setCardErrors((prev) => ({ ...prev, [solicitud.id]: null }))
    try {
      await cancelarSolicitud(solicitud.id)
      refetch()
    } catch (err) {
      setCardErrors((prev) => ({
        ...prev,
        [solicitud.id]: parseApiError(err),
      }))
    } finally {
      setActionLoadingId(null)
    }
  }

  return (
    <main className="page-container">
      <header className="page-header">
        <h1>Operaciones de alquiler</h1>
        <p>
          Gestiona solicitudes pendientes. Al confirmar o cancelar se sincroniza
          el estado del vehículo en el catálogo.
        </p>
      </header>

      <div className={styles.filter}>
        <label htmlFor="estadoSolicitud">Filtrar por estado</label>
        <select
          id="estadoSolicitud"
          value={estadoFiltro}
          onChange={(e) => setEstadoFiltro(e.target.value)}
        >
          <option value="">Todas</option>
          {ESTADOS_SOLICITUD.map((estado) => (
            <option key={estado} value={estado}>
              {estado}
            </option>
          ))}
        </select>
      </div>

      {loading && <LoadingSpinner label="Cargando solicitudes..." />}
      {error && <ErrorMessage message={error} onRetry={refetch} />}

      {!loading && !error && data.length === 0 && (
        <div className="empty-state">
          <p>No hay solicitudes con el filtro seleccionado.</p>
        </div>
      )}

      {!loading && !error && data.length > 0 && (
        <div className="grid-cards">
          {data.map((solicitud) => (
            <SolicitudCard
              key={solicitud.id}
              solicitud={solicitud}
              onConfirmar={handleConfirmar}
              onCancelar={handleCancelar}
              actionLoading={actionLoadingId === solicitud.id}
              actionError={cardErrors[solicitud.id]}
            />
          ))}
        </div>
      )}
    </main>
  )
}
