import { useCallback, useEffect, useState } from 'react'
import axios from 'axios'
import { listarVehiculos, parseApiError } from '../services/vehiculosService.js'

export function useVehiculos(filtros = {}) {
  const marca = filtros.marca ?? ''
  const modelo = filtros.modelo ?? ''
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

    async function fetchVehiculos() {
      setLoading(true)
      setError(null)
      try {
        const result = await listarVehiculos(
          { marca, modelo, estado },
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

    fetchVehiculos()
    return () => controller.abort()
  }, [marca, modelo, estado, refreshKey])

  return { data, loading, error, refetch }
}
