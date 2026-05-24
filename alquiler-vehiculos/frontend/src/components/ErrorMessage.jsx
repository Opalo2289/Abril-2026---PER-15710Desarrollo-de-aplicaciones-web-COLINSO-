import styles from './ErrorMessage.module.css'

export default function ErrorMessage({ message, onRetry }) {
  if (!message) return null

  return (
    <div className={styles.error} role="alert">
      <p className={styles.text}>{message}</p>
      {onRetry && (
        <button type="button" className={styles.retry} onClick={onRetry}>
          Reintentar
        </button>
      )}
    </div>
  )
}
