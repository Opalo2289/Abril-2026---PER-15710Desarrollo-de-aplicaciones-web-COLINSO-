import { useState } from 'react'
import { ESTADOS_VEHICULO } from '../constants/enums.js'
import styles from './VehicleForm.module.css'

const EMPTY_VALUES = {
  marca: '',
  modelo: '',
  matricula: '',
  estado: 'DISPONIBLE',
  precioPorDia: '',
}

function validate(values) {
  const errors = {}
  if (!values.marca?.trim()) errors.marca = 'La marca es obligatoria'
  if (!values.modelo?.trim()) errors.modelo = 'El modelo es obligatorio'
  if (!values.matricula?.trim()) errors.matricula = 'La matrícula es obligatoria'
  if (!ESTADOS_VEHICULO.includes(values.estado)) {
    errors.estado = 'Selecciona un estado válido'
  }
  const precio = Number(values.precioPorDia)
  if (values.precioPorDia === '' || Number.isNaN(precio)) {
    errors.precioPorDia = 'El precio por día es obligatorio'
  } else if (precio <= 0) {
    errors.precioPorDia = 'El precio debe ser mayor que cero'
  }
  return errors
}

export default function VehicleForm({
  initialValues,
  onSubmit,
  submitLabel = 'Guardar',
  disabled = false,
}) {
  const [values, setValues] = useState({
    ...EMPTY_VALUES,
    ...initialValues,
    precioPorDia:
      initialValues?.precioPorDia != null
        ? String(initialValues.precioPorDia)
        : '',
  })
  const [errors, setErrors] = useState({})

  function handleChange(event) {
    const { name, value } = event.target
    setValues((prev) => ({ ...prev, [name]: value }))
    setErrors((prev) => ({ ...prev, [name]: undefined }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const validation = validate(values)
    setErrors(validation)
    if (Object.keys(validation).length > 0) return

    await onSubmit({
      marca: values.marca.trim(),
      modelo: values.modelo.trim(),
      matricula: values.matricula.trim(),
      estado: values.estado,
      precioPorDia: Number(values.precioPorDia),
    })
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <div className={styles.field}>
        <label htmlFor="marca">Marca</label>
        <input
          id="marca"
          name="marca"
          value={values.marca}
          onChange={handleChange}
          disabled={disabled}
        />
        {errors.marca && <span className={styles.error}>{errors.marca}</span>}
      </div>

      <div className={styles.field}>
        <label htmlFor="modelo">Modelo</label>
        <input
          id="modelo"
          name="modelo"
          value={values.modelo}
          onChange={handleChange}
          disabled={disabled}
        />
        {errors.modelo && <span className={styles.error}>{errors.modelo}</span>}
      </div>

      <div className={styles.field}>
        <label htmlFor="matricula">Matrícula</label>
        <input
          id="matricula"
          name="matricula"
          value={values.matricula}
          onChange={handleChange}
          disabled={disabled}
        />
        {errors.matricula && (
          <span className={styles.error}>{errors.matricula}</span>
        )}
      </div>

      <div className={styles.field}>
        <label htmlFor="estado">Estado</label>
        <select
          id="estado"
          name="estado"
          value={values.estado}
          onChange={handleChange}
          disabled={disabled}
        >
          {ESTADOS_VEHICULO.map((estado) => (
            <option key={estado} value={estado}>
              {estado}
            </option>
          ))}
        </select>
        {errors.estado && <span className={styles.error}>{errors.estado}</span>}
      </div>

      <div className={styles.field}>
        <label htmlFor="precioPorDia">Precio por día (€)</label>
        <input
          id="precioPorDia"
          name="precioPorDia"
          type="number"
          min="0.01"
          step="0.01"
          value={values.precioPorDia}
          onChange={handleChange}
          disabled={disabled}
        />
        {errors.precioPorDia && (
          <span className={styles.error}>{errors.precioPorDia}</span>
        )}
      </div>

      <button type="submit" className={styles.submit} disabled={disabled}>
        {submitLabel}
      </button>
    </form>
  )
}
