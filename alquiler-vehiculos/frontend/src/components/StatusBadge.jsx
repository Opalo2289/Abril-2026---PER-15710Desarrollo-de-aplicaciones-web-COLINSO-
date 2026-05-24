import styles from './StatusBadge.module.css'

const ESTADO_CLASS = {
  DISPONIBLE: styles.disponible,
  ALQUILADO: styles.alquilado,
  RESERVADO: styles.reservado,
  MANTENIMIENTO: styles.mantenimiento,
  PENDIENTE: styles.pendiente,
  CONFIRMADA: styles.confirmada,
  CANCELADA: styles.cancelada,
}

export default function StatusBadge({ estado }) {
  const className = ESTADO_CLASS[estado] || styles.default
  return (
    <span className={`${styles.badge} ${className}`}>
      {estado || '—'}
    </span>
  )
}
