package com.crearcode.leads.infraestructura;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita {@code @Async} y define el único ejecutor que usa: hilos
 * virtuales, uno por tarea.
 *
 * <p>
 * Las tareas que van aquí son envíos de correo: esperan a un servidor
 * SMTP y no calculan nada. Un hilo virtual bloqueado no ocupa un hilo
 * del sistema operativo, así que no hace falta dimensionar un pool ni
 * decidir qué pasa cuando se llena — el problema que un pool fijo
 * resuelve (limitar el paralelismo de CPU) aquí no existe.
 *
 * <p>
 * Se declara un ejecutor propio en vez de encender
 * {@code spring.threads.virtual.enabled}: esa bandera cambiaría también
 * el ejecutor de Tomcat, que es un cambio de comportamiento de todo el
 * servidor y no lo que se quería resolver.
 */
@Configuration
@EnableAsync
class ConfiguracionDeTareasAsincronas {

	/**
	 * El nombre del bean es el del método, y es el que cita
	 * {@code @Async} en {@code NotificarSolicitudRegistrada}: la
	 * aplicación no puede importar infraestructura (ArchUnit), así que
	 * allí va como literal. Si se renombra este método, hay que
	 * renombrarlo allí.
	 */
	@Bean
	AsyncTaskExecutor ejecutorDeNotificaciones() {
		return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
	}

}
