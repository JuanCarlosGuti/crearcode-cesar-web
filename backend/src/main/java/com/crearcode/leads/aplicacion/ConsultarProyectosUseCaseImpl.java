package com.crearcode.leads.aplicacion;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crearcode.leads.dominio.ConsultarProyectosUseCase;
import com.crearcode.leads.dominio.Correo;
import com.crearcode.leads.dominio.CotizacionId;
import com.crearcode.leads.dominio.Proyecto;
import com.crearcode.leads.dominio.ProyectoId;
import com.crearcode.leads.dominio.ProyectoRepositorio;
import com.crearcode.leads.dominio.UsuarioRepositorio;

@Service
class ConsultarProyectosUseCaseImpl implements ConsultarProyectosUseCase {

	private final ProyectoRepositorio proyectos;
	private final UsuarioRepositorio usuarios;

	ConsultarProyectosUseCaseImpl(ProyectoRepositorio proyectos, UsuarioRepositorio usuarios) {
		this.proyectos = proyectos;
		this.usuarios = usuarios;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Proyecto> listar() {
		return proyectos.listar();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Proyecto> obtener(ProyectoId id) {
		return proyectos.buscarPorId(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Proyecto> listarDe(Correo correoDelCliente) {
		return proyectos.listarPorCorreoDelCliente(correoDelCliente);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Proyecto> obtenerDe(ProyectoId id, Correo correoDelCliente) {
		return proyectos.buscarPorId(id).filter(proyecto -> proyecto.perteneceA(correoDelCliente));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Proyecto> deLaCotizacion(CotizacionId cotizacion) {
		return proyectos.buscarPorCotizacion(cotizacion);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean clienteTieneCuenta(Correo correoDelCliente) {
		return usuarios.buscarPorCorreo(correoDelCliente).isPresent();
	}

}
