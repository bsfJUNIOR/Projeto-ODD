package servicos;

import entidades.NivelAcesso;
import entidades.Usuario;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import persistencia.HibernateUtil;
import seguranca.SenhaUtil;

/** Operações de usuário, mantendo a persistência no padrão Hibernate já usado no projeto. */
public class UsuarioService {

    public Usuario autenticar(String nomeUsuario, char[] senha) {
        try {
            Usuario usuario = (Usuario) HibernateUtil.getSession()
                    .createQuery("FROM Usuario WHERE usuario = :usuario")
                    .setString("usuario", nomeUsuario.trim())
                    .uniqueResult();
            return usuario != null && SenhaUtil.confere(senha, usuario.getSenha()) ? usuario : null;
        } finally {
            HibernateUtil.closeSession();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Usuario> listar() {
        try {
            return HibernateUtil.getSession().createQuery("FROM Usuario ORDER BY usuario").list();
        } finally {
            HibernateUtil.closeSession();
        }
    }

    public void salvar(Usuario usuario, char[] novaSenha) throws IllegalArgumentException {
        validar(usuario, novaSenha);
        try {
            HibernateUtil.beginTransaction();
            if (novaSenha != null && novaSenha.length > 0) {
                usuario.setSenha(SenhaUtil.gerarHash(novaSenha));
            }
            if (usuario.getId() == null) {
                HibernateUtil.getSession().persist(usuario);
            } else {
                HibernateUtil.getSession().merge(usuario);
            }
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
        try {
            HibernateUtil.beginTransaction();
            Usuario gerenciado = (Usuario) HibernateUtil.getSession().get(Usuario.class, usuario.getId());
            if (gerenciado != null) {
                HibernateUtil.getSession().delete(gerenciado);
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
            Long total = (Long) HibernateUtil.getSession()
                    .createQuery("SELECT COUNT(u) FROM Usuario u").uniqueResult();
            if (total != null && total == 0) {
                Usuario admin = new Usuario();
                admin.setUsuario("admin");
                admin.setNivelAcesso(NivelAcesso.ADMIN);
                salvar(admin, "admin".toCharArray());
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
}
