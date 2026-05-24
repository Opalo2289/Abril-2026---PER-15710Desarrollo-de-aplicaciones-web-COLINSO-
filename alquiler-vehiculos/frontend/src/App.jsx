import { lazy, Suspense } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import LoadingSpinner from './components/LoadingSpinner.jsx'

const HomePage = lazy(() => import('./pages/HomePage.jsx'))
const VehiculosPage = lazy(() => import('./pages/VehiculosPage.jsx'))
const VehiculoDetallePage = lazy(() => import('./pages/VehiculoDetallePage.jsx'))
const AdminPage = lazy(() => import('./pages/AdminPage.jsx'))
const OperacionesPage = lazy(() => import('./pages/OperacionesPage.jsx'))

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Suspense fallback={<LoadingSpinner label="Cargando página..." />}>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/vehiculos" element={<VehiculosPage />} />
          <Route path="/vehiculos/:id" element={<VehiculoDetallePage />} />
          <Route path="/admin" element={<AdminPage />} />
          <Route path="/operaciones" element={<OperacionesPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  )
}
