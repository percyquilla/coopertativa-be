package com.bien.usuario.service;

import com.bien.usuario.entity.Usuario;
import com.bien.usuario.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

/**
 * Registra el resultado de cada intento de ingreso en su propia transacción.
 * <p>
 * Antes el incremento de intentos fallidos vivía dentro del método
 * {@code login()}, que es {@code @Transactional} y termina lanzando excepción
 * cuando la contraseña no coincide: el rollback descartaba el incremento y el
 * contador nunca subía, así que el bloqueo por intentos no se activaba jamás.
 * Aislar la escritura en {@code REQUIRES_NEW} hace que se confirme aunque el
 * login falle después. Es un bean aparte a propósito: si estos métodos
 * estuvieran en {@code AuthService}, la auto-invocación saltaría el interceptor
 * de transacciones.
 */
@ApplicationScoped
public class LoginLedger {

    @Inject
    UsuarioRepository usuarioRepository;

    /** Suma un intento fallido y bloquea la cuenta si alcanza el máximo. */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void registrarFallo(Long usuarioId, int maxIntentos) {
        Usuario u = usuarioRepository.findById(usuarioId);
        if (u == null) {
            return;
        }
        int intentos = u.getIntentosFallidos() + 1;
        u.setIntentosFallidos((short) intentos);
        if (maxIntentos > 0 && intentos >= maxIntentos) {
            u.setEstado("B");
            u.setFechaBloqueo(LocalDateTime.now());
            u.setMotivoBloqueo("Bloqueado por " + intentos + " intentos de ingreso fallidos");
        }
    }

    /**
     * Marca un ingreso exitoso: reinicia el contador, sella el último acceso y,
     * si la contraseña está vencida, deja pedido el cambio obligatorio.
     * Devuelve si el usuario debe cambiar la contraseña en esta sesión.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public boolean registrarAccesoOk(Long usuarioId, boolean passwordVencida) {
        Usuario u = usuarioRepository.findById(usuarioId);
        u.setIntentosFallidos((short) 0);
        u.setFechaUltimoAcceso(LocalDateTime.now());
        if (passwordVencida) {
            u.setRequiereCambioPwd(true);
        }
        return u.isRequiereCambioPwd();
    }
}
