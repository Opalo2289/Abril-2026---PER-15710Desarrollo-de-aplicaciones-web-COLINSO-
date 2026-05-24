import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { listarSolicitudes, parseApiError } from '../services/operacionesService.js'

export function useSolicitudes(filtros = {}) {
  const estado = filtros.estado ?? ''

  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  const refetch = useCallback(() => {
    setRefreshKey((key) => key + 1)
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    async function fetchSolicitudes() {
      setLoading(true)
      setError(null)
      try {
        const result = await listarSolicitudes(
          { estado },
          { signal: controller.signal },
        )
        setData(result)
      } catch (err) {
        if (axios.isCancel(err)) return
        setError(parseApiError(err))
        setData([])
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    fetchSolicitudes()
    return () => controller.abort()
  }, [estado, refreshKey])

  return { data, loading, error, refetch }
}
