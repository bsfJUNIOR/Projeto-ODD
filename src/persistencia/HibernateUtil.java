package persistencia;


import entidades.Despesa;
import entidades.Receita;
import entidades.Usuario;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;

public class HibernateUtil {

    private static final SessionFactory factory;
    private static final ThreadLocal<Session> sessionThread = new ThreadLocal<Session>();
    private static final ThreadLocal<Transaction> transactionThread = new ThreadLocal<Transaction>();

    public static Session getSession() {
        Session session = sessionThread.get();
        if ((session == null) || (!(session.isOpen()))) {
            session = factory.openSession();
            sessionThread.set(session);
        }
        return sessionThread.get();
    }

    public static void closeSession() {
        Session session = sessionThread.get();
        if ((session != null) && (session.isOpen())) {
            sessionThread.remove();
            session.close();
        }
    }

    public static void beginTransaction() {
        Transaction transaction = getSession().beginTransaction();
        transactionThread.set(transaction);
    }

    public static void commitTransaction() {
        Transaction transaction = transactionThread.get();
        if ((transaction != null) && (!(transaction.wasCommitted())) && (!(transaction.wasRolledBack()))) {
            transaction.commit();
            transactionThread.remove();
        }
    }

    public static void rollbackTransaction() {
        Transaction transaction = transactionThread.get();
        if ((transaction != null) && (!(transaction.wasCommitted())) && (!(transaction.wasRolledBack()))) {
            transaction.rollback();
            transactionThread.remove();
        }
    }
//configuração do hibernate para conexão com banco de dados

    static {
        try {
            factory = new AnnotationConfiguration()
//                  .setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
//                  .setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver")
//                  .setProperty("hibernate.connection.url", "jdbc:mysql://localhost:3306/odd?useSSL=false&serverTimezone=UTC")
                    //Dialeto do banco de dados.
                    .setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
//                  //Drive JDBC do banco de dados.
                    .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
//                  //url do banco, indica o caminho onde se encontra o banco de dados. Local, porta e nome do banco.
                    .setProperty("hibernate.connection.url", "jdbc:postgresql://localhost:5432/Odd")
//                  //usuário do banco de dados
                    .setProperty("hibernate.connection.username", "postgres")
                    //senha do banco de dados
                    .setProperty("hibernate.connection.password", "123")
                    .setProperty("hibernate.hbm2ddl.auto", "update")
//                    .setProperty("hibernate.c3p0.max_size", "10")
//                    .setProperty("hibernate.c3p0.min_size", "2")
                    .setProperty("hibernate.c3p0.timeout", "5000")
                    .setProperty("hibernate.c3p0.max_statements", "10")
                    .setProperty("hibernate.c3p0.idle_test_period", "3000")
                    .setProperty("hibernate.c3p0.acquire_increment", "2")
                    .setProperty("use_outer_join", "true")
                    .setProperty("hibernate.generate_statistics", "true")
                    .setProperty("hibernate.use_sql_comments", "true")
                    .setProperty("show_sql", "true")
                    .setProperty("hibernate.format_sql", "true")
                    .addAnnotatedClass(Despesa.class) 
                    .addAnnotatedClass(Receita.class) 
                    .addAnnotatedClass(Usuario.class)
                    .buildSessionFactory();
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
