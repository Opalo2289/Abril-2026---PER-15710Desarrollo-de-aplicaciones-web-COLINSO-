import { useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import VehicleCard from '../components/VehicleCard.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useVehiculos } from '../hooks/useVehiculos.js'
import { ESTADOS_VEHICULO } from '../constants/enums.js'
import styles from './VehiculosPage.module.css'

export default function VehiculosPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [marcaInput, setMarcaInput] = useState(searchParams.get('marca') || '')

  const filtros = useMemo(
    () => ({
      marca: searchParams.get('marca') || '',
      estado: searchParams.get('estado') || '',
    }),
    [searchParams],
  )

  const { data, loading, error, refetch } = useVehiculos(filtros)

  function applyFilters(event) {
    event.preventDefault()
    const formData = new FormData(event.currentTarget)
    const next = new URLSearchParams()
    if (marcaInput.trim()) next.set('marca', marcaInput.trim())
    const estado = formData.get('estado')
    if (estado) next.set('estado', estado)
    setSearchParams(next)
  }

  function clearFilters() {
    setMarcaInput('')
    setSearchParams({})
  }

  return (
    <main className="page-container">
      <header className="page-header">
        <h1>Catálogo de vehículos</h1>
        <p>Filtra por marca o estado para encontrar el vehículo adecuado.</p>
      </header>

      <form className={styles.filters} onSubmit={applyFilters}>
        <div className={styles.field}>
          <label htmlFor="marca">Marca</label>
          <input
            id="marca"
            name="marca"
            value={marcaInput}
            onChange={(e) => setMarcaInput(e.target.value)}
            placeholder="Ej. Toyota"
          />
        </div>
        <div className={styles.field}>
          <label htmlFor="estado">Estado</label>
          <select
            id="estado"
            name="estado"
            defaultValue={filtros.estado}
            key={filtros.estado}
          >
            <option value="">Todos</option>
            {ESTADOS_VEHICULO.map((estado) => (
              <option key={estado} value={estado}>
                {estado}
              </option>
            ))}
          </select>
        </div>
        <div className={styles.actions}>
          <button type="submit" className={styles.apply}>
            Aplicar filtros
          </button>
          <button type="button" className={styles.clear} onClick={clearFilters}>
            Limpiar
          </button>
        </div>
      </form>

      {loading && <LoadingSpinner label="Cargando vehículos..." />}
      {error && <ErrorMessage message={error} onRetry={refetch} />}

      {!loading && !error && data.length === 0 && (
        <div className="empty-state">
          <p>No hay vehículos que coincidan con los filtros seleccionados.</p>
        </div>
      )}

      {!loading && !error && data.length > 0 && (
        <div className="grid-cards">
          {data.map((vehiculo) => (
            <VehicleCard key={vehiculo.id} vehiculo={vehiculo} />
          ))}
        </div>
      )}
    </main>
  )
}
