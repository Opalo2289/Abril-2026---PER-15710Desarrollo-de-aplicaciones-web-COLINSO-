import styles from './LoadingSpinner.module.css'

export default function LoadingSpinner({ label = 'Cargando...' }) {
  return (
    <div className={styles.wrapper} role="status" aria-live="polite">
      <div className={styles.spinner} aria-hidden="true" />
      <span className={styles.label}>{label}</span>
    </div>
  )
}
