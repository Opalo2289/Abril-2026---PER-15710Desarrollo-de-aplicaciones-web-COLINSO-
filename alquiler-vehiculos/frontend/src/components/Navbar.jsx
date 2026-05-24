import { NavLink } from 'react-router-dom'
import styles from './Navbar.module.css'

const NAV_ITEMS = [
  { to: '/', label: 'Inicio', end: true },
  { to: '/vehiculos', label: 'Vehículos' },
  { to: '/operaciones', label: 'Operaciones' },
  { to: '/admin', label: 'Admin' },
]

export default function Navbar() {
  return (
    <header className={styles.header}>
      <nav className={styles.nav} aria-label="Principal">
        <NavLink to="/" className={styles.brand} end>
          Alquiler Vehículos
        </NavLink>
        <ul className={styles.list}>
          {NAV_ITEMS.map((item) => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  isActive ? `${styles.link} ${styles.active}` : styles.link
                }
              >
                {item.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </header>
  )
}
