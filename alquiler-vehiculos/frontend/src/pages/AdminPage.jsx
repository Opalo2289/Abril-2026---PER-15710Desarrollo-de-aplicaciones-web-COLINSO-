import { useCallback, useState } from 'react'
import VehicleForm from '../components/VehicleForm.jsx'
import StatusBadge from '../components/StatusBadge.jsx'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { useVehiculos } from '../hooks/useVehiculos.js'
import {
  crearVehiculo,
  actualizarVehiculo,
  eliminarVehiculo,
  vehiculoToPayload,
  parseApiError,
} from '../services/vehiculosService.js'
import styles from './AdminPage.module.css'

export default function AdminPage() {
  const { data, loading, error, refetch } = useVehiculos({})
  const [editing, setEditing] = useState(null)
  const [feedback, setFeedback] = useState(null)
  const [mutationError, setMutationError] = useState(null)
  const [saving, setSaving] = useState(false)

  const showSuccess = useCallback((message) => {
    setFeedback({ type: 'success', message })
    setMutationError(null)
  }, [])

  async function handleCreate(payload) {
    setSaving(true)
    setMutationError(null)
    try {
      await crearVehiculo(payload)
      showSuccess('Vehículo creado correctamente.')
      refetch()
    } catch (err) {
      setMutationError(parseApiError(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleUpdate(payload) {
    setSaving(true)
    setMutationError(null)
    try {
      await actualizarVehiculo(editing.id, payload)
      showSuccess('Vehículo actualizado correctamente.')
      setEditing(null)
      refetch()
    } catch (err) {
      setMutationError(parseApiError(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(vehiculo) {
    if (!window.confirm(`¿Eliminar ${vehiculo.marca} ${vehiculo.modelo}?`)) {
      return
    }
    setMutationError(null)
    try {
      await eliminarVehiculo(vehiculo.id)
      showSuccess('Vehículo eliminado.')
      if (editing?.id === vehiculo.id) setEditing(null)
      refetch()
    } catch (err) {
      setMutationError(parseApiError(err))
    }
  }

  return (
    <main className="page-container">
      <header className="page-header">
        <h1>Panel de administración</h1>
        <p>Crea, edita o elimina vehículos del catálogo.</p>
      </header>

      {feedback && (
        <div
          className={`feedback-banner feedback-banner--${feedback.type}`}
          role="status"
        >
          {feedback.message}
        </div>
      )}
      {mutationError && (
        <div className="feedback-banner feedback-banner--error" role="alert">
          {mutationError}
        </div>
      )}

      <div className={styles.layout}>
        <section>
          <h2>{editing ? 'Editar vehículo' : 'Nuevo vehículo'}</h2>
          <VehicleForm
            key={editing?.id ?? 'new'}
            initialValues={editing ? vehiculoToPayload(editing) : undefined}
            onSubmit={editing ? handleUpdate : handleCreate}
            submitLabel={editing ? 'Guardar cambios' : 'Crear vehículo'}
            disabled={saving}
          />
          {editing && (
            <button
              type="button"
              className={styles.cancelEdit}
              onClick={() => setEditing(null)}
            >
              Cancelar edición
            </button>
          )}
        </section>

        <section>
          <h2>Inventario</h2>
          {loading && <LoadingSpinner label="Cargando inventario..." />}
          {error && <ErrorMessage message={error} onRetry={refetch} />}

          {!loading && !error && data.length === 0 && (
            <div className="empty-state">
              <p>No hay vehículos registrados. Crea el primero con el formulario.</p>
            </div>
          )}

          {!loading && !error && data.length > 0 && (
            <div className={styles.tableWrap}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>Marca / Modelo</th>
                    <th>Matrícula</th>
                    <th>Estado</th>
                    <th>Precio/día</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {data.map((vehiculo) => (
                    <tr key={vehiculo.id}>
                      <td>
                        {vehiculo.marca} {vehiculo.modelo}
                      </td>
                      <td>{vehiculo.matricula}</td>
                      <td>
                        <StatusBadge estado={vehiculo.estado} />
                      </td>
                      <td>{vehiculo.precioPorDia} €</td>
                      <td className={styles.rowActions}>
                        <button
                          type="button"
                          onClick={() => {
                            setEditing(vehiculo)
                            setFeedback(null)
                          }}
                        >
                          Editar
                        </button>
                        <button
                          type="button"
                          className={styles.delete}
                          onClick={() => handleDelete(vehiculo)}
                        >
                          Eliminar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>
      </div>
    </main>
  )
}
