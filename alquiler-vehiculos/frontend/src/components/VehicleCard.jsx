import { Link } from 'react-router-dom'
import StatusBadge from './StatusBadge.jsx'
import styles from './VehicleCard.module.css'

function formatPrecio(precio) {
  if (precio == null) return '—'
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
  }).format(Number(precio))
}

export default function VehicleCard({ vehiculo }) {
  return (
    <article className={styles.card}>
      <header className={styles.header}>
        <h3 className={styles.title}>
          {vehiculo.marca} {vehiculo.modelo}
        </h3>
        <StatusBadge estado={vehiculo.estado} />
      </header>
      <dl className={styles.details}>
        <div>
          <dt>Matrícula</dt>
          <dd>{vehiculo.matricula}</dd>
        </div>
        <div>
          <dt>Precio / día</dt>
          <dd>{formatPrecio(vehiculo.precioPorDia)}</dd>
        </div>
      </dl>
      <Link to={`/vehiculos/${vehiculo.id}`} className={styles.link}>
        Ver detalle
      </Link>
    </article>
  )
}
