/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sistemaodd;

import javax.swing.SwingUtilities;
import servicos.UsuarioService;
import telas.Login;
/**
 *
 * @author Usuario
 */
public class SistemaODD {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new UsuarioService().garantirAdministradorInicial();
                    new Login().setVisible(true);
                } catch (RuntimeException e) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Não foi possível iniciar o sistema. Verifique a conexão com o banco de dados.",
                            "ODD", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
