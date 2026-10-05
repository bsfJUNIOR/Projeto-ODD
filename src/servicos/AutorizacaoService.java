package servicos;

import sessao.SessaoUsuario;

/** Centraliza as permissões da aplicação local. */
public final class AutorizacaoService {

    private AutorizacaoService() { }

    public static void exigirAutenticado() {
        if (!SessaoUsuario.estaAutenticado()) {
            throw new SecurityException("É necessário realizar login.");
        }
    }

    public static void exigirFinanceiro() {
        exigirAutenticado();
    }

    public static void exigirAdministrador() {
        exigirAutenticado();
        if (!SessaoUsuario.ehAdministrador()) {
            throw new SecurityException("Acesso permitido somente para administradores.");
        }
    }
}
