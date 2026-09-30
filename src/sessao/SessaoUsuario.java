package sessao;

import entidades.NivelAcesso;
import entidades.Usuario;

/** Mantém somente os dados necessários do usuário autenticado na aplicação local. */
public final class SessaoUsuario {

    private static Long id;
    private static String usuario;
    private static NivelAcesso nivelAcesso;

    private SessaoUsuario() { }

    public static void iniciar(Usuario usuarioAutenticado) {
        id = usuarioAutenticado.getId();
        usuario = usuarioAutenticado.getUsuario();
        nivelAcesso = usuarioAutenticado.getNivelAcesso();
    }

    public static void encerrar() {
        id = null;
        usuario = null;
        nivelAcesso = null;
    }

    public static boolean estaAutenticado() { return id != null; }
    public static Long getId() { return id; }
    public static String getUsuario() { return usuario; }
    public static NivelAcesso getNivelAcesso() { return nivelAcesso; }
    public static boolean ehAdministrador() { return nivelAcesso == NivelAcesso.ADMIN; }
}
