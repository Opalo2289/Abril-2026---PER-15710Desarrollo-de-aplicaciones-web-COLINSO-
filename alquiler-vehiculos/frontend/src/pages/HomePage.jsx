import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import LoadingSpinner from '../components/LoadingSpinner.jsx'
import ErrorMessage from '../components/ErrorMessage.jsx'
import { listarVehiculos } from '../services/vehiculosService.js'
import { listarSolicitudes } from '../services/operacionesService.js'
import { parseApiError } from '../services/httpClient.js'
import styles from './HomePage.module.css'

export default function HomePage() {
  const [stats, setStats] = useState({
    disponibles: 0,
    pendientes: 0,
    total: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const controller = new AbortController()

    async function loadStats() {
      setLoading(true)
      setError(null)
      try {
        const [vehiculos, solicitudes] = await Promise.all([
          listarVehiculos({}, { signal: controller.signal }),
          listarSolicitudes({ estado: 'PENDIENTE' }, { signal: controller.signal }),
        ])

        const disponibles = vehiculos.filter((v) => v.estado === 'DISPONIBLE').length

        setStats({
          disponibles,
          pendientes: solicitudes.length,
          total: vehiculos.length,
        })
      } catch (err) {
        if (controller.signal.aborted) return
        setError(parseApiError(err))
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadStats()
    return () => controller.abort()
  }, [])

  if (loading) {
    return <LoadingSpinner label="Cargando resumen del sistema..." />
  }

  if (error) {
    return <ErrorMessage message={error} />
  }

  return (
    <main className="page-container">
      <section className={styles.hero}>
        <h1>Sistema de alquiler de vehículos</h1>
        <p>
          Consulta el catálogo, gestiona solicitudes de alquiler y administra el
          inventario desde una interfaz React conectada al API Gateway.
        </p>
        <div className={styles.cta}>
          <Link to="/vehiculos" className={styles.primary}>
            Ver catálogo
          </Link>
          <Link to="/operaciones" className={styles.secondary}>
            Ver solicitudes
          </Link>
        </div>
      </section>

      <section className={styles.stats} aria-label="Estadísticas rápidas">
        <article className={styles.statCard}>
          <span className={styles.statValue}>{stats.disponibles}</span>
          <span className={styles.statLabel}>Vehículos disponibles</span>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statValue}>{stats.pendientes}</span>
          <span className={styles.statLabel}>Solicitudes pendientes</span>
        </article>
        <article className={styles.statCard}>
          <span className={styles.statValue}>{stats.total}</span>
          <span className={styles.statLabel}>Total en catálogo</span>
        </article>
      </section>
    </main>
  )
}
