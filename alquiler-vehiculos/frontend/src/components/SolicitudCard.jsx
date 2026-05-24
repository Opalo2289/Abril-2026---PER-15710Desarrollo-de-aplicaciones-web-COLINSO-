import { Link } from 'react-router-dom'
import StatusBadge from './StatusBadge.jsx'
import styles from './SolicitudCard.module.css'

export default function SolicitudCard({
  solicitud,
  onConfirmar,
  onCancelar,
  actionLoading = false,
  actionError = null,
}) {
  const isPendiente = solicitud.estado === 'PENDIENTE'

  return (
    <article className={styles.card}>
      <header className={styles.header}>
        <h3 className={styles.title}>Solicitud #{solicitud.id}</h3>
        <StatusBadge estado={solicitud.estado} />
      </header>

      <dl className={styles.details}>
        <div>
          <dt>Vehículo</dt>
          <dd>
            <Link to={`/vehiculos/${solicitud.vehiculoId}`}>
              ID {solicitud.vehiculoId}
            </Link>
          </dd>
        </div>
        <div>
          <dt>Periodo</dt>
          <dd>
            {solicitud.fechaInicio} → {solicitud.fechaFin}
          </dd>
        </div>
      </dl>

      {actionError && (
        <p className={styles.actionError} role="alert">
          {actionError}
        </p>
      )}

      {isPendiente && (
        <div className={styles.actions}>
          <button
            type="button"
            className={styles.confirm}
            disabled={actionLoading}
            onClick={() => onConfirmar(solicitud)}
          >
            Confirmar
          </button>
          <button
            type="button"
            className={styles.cancel}
            disabled={actionLoading}
            onClick={() => onCancelar(solicitud)}
          >
            Cancelar
          </button>
        </div>
      )}
    </article>
  )
}
