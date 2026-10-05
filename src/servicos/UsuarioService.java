package servicos;

import entidades.NivelAcesso;
import entidades.Usuario;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import persistencia.HibernateUtil;
import seguranca.SenhaUtil;
import sessao.SessaoUsuario;

/** Operações de usuário, mantendo a persistência no padrão Hibernate já usado no projeto. */
public class UsuarioService {

    private final HistoricoService historicoService = new HistoricoService();

    public Usuario autenticar(String nomeUsuario, char[] senha) {
        try {
            Usuario usuario = (Usuario) HibernateUtil.getSession().createQuery("FROM Usuario WHERE usuario = :usuario").setString("usuario", nomeUsuario.trim()).uniqueResult();
            return usuario != null && usuario.isAtivo() && SenhaUtil.confere(senha, usuario.getSenha()) ? usuario : null;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Usuario> listar() {
        AutorizacaoService.exigirAdministrador();
        try {
            return HibernateUtil.getSession().createQuery("FROM Usuario WHERE (ativo = true OR ativo IS NULL) ORDER BY usuario").list();
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void salvar(Usuario usuario, char[] novaSenha) throws IllegalArgumentException {
        AutorizacaoService.exigirAdministrador();
        validar(usuario, novaSenha);
        boolean novo = usuario.getId() == null;
        try {
            HibernateUtil.beginTransaction();
            if (novaSenha != null && novaSenha.length > 0) {
                usuario.setSenha(SenhaUtil.gerarHash(novaSenha));
            }
            if (novo) {
                usuario.setAtivo(true);
                salvarEntidade(usuario);
            } else {
                salvarEntidade(usuario);
            }
            historicoService.registrarNaTransacao(novo ? "CADASTRO" : "EDICAO",
                    (novo ? "cadastrou o usuário " : "editou o usuário ") + usuario.getUsuario() + ".");
            HibernateUtil.commitTransaction();
        } catch (ConstraintViolationException e) {
            HibernateUtil.rollbackTransaction();
            throw new IllegalArgumentException("Já existe um usuário com esse nome.");
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void excluir(Usuario usuario) {
        AutorizacaoService.exigirAdministrador();
        if (usuario.getId().equals(SessaoUsuario.getId())) {
            throw new SecurityException("Não é permitido excluir o usuário autenticado.");
        }
        try {
            HibernateUtil.beginTransaction();
            Usuario gerenciado = (Usuario) HibernateUtil.getSession().get(Usuario.class, usuario.getId());
            if (gerenciado != null) {
                gerenciado.setAtivo(false);
                salvarEntidade(gerenciado);
                historicoService.registrarNaTransacao("EXCLUSAO", "excluiu o usuário " + gerenciado.getUsuario() + ".");
            }
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void garantirAdministradorInicial() {
        try {
            Long total = (Long) HibernateUtil.getSession().createQuery("SELECT COUNT(u) FROM Usuario u WHERE (ativo = true OR ativo IS NULL)").uniqueResult();
            if (total != null && total == 0) {
                Usuario admin = new Usuario();
                admin.setUsuario("admin");
                admin.setNivelAcesso(NivelAcesso.ADMIN);
                salvarAdministradorInicial(admin);
            }
        } finally {
            HibernateUtil.closeSession();
        }
    }

    private void validar(Usuario usuario, char[] novaSenha) {
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().length() == 0) {
            throw new IllegalArgumentException("O usuário é obrigatório.");
        }
        if (usuario.getNivelAcesso() == null) {
            throw new IllegalArgumentException("O nível de acesso é obrigatório.");
        }
        if (usuario.getId() == null && (novaSenha == null || novaSenha.length == 0)) {
            throw new IllegalArgumentException("A senha é obrigatória.");
        }
    }

    private void salvarAdministradorInicial(Usuario admin) {
        try {
            HibernateUtil.beginTransaction();
            admin.setSenha(SenhaUtil.gerarHash("admin".toCharArray()));
            admin.setAtivo(true);
            salvarEntidade(admin);
            HibernateUtil.commitTransaction();
        } catch (RuntimeException e) {
            HibernateUtil.rollbackTransaction();
            throw e;
        } finally {
            HibernateUtil.closeSession();
        }
    }
    private void salvarEntidade(Usuario u){if(u.getId()==null)HibernateUtil.getSession().persist(u);else HibernateUtil.getSession().merge(u);}
}
