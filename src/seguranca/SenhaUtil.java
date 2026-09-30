package seguranca;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Gera e valida hashes PBKDF2 sem dependências adicionais. */
public final class SenhaUtil {

    private static final int ITERACOES = 10000;
    private static final int TAMANHO_CHAVE = 256;

    private SenhaUtil() { }

    public static String gerarHash(char[] senha) {
        byte[] sal = new byte[16];
        new SecureRandom().nextBytes(sal);
        byte[] hash = gerarPbkdf2(senha, sal, ITERACOES);
        return ITERACOES + ":" + paraHexadecimal(sal)
                + ":" + paraHexadecimal(hash);
    }

    public static boolean confere(char[] senha, String hashArmazenado) {
        if (hashArmazenado == null) {
            return false;
        }
        try {
            String[] partes = hashArmazenado.split(":");
            if (partes.length != 3) {
                return false;
            }
            int iteracoes = Integer.parseInt(partes[0]);
            byte[] sal = deHexadecimal(partes[1]);
            byte[] hashEsperado = deHexadecimal(partes[2]);
            byte[] hashInformado = gerarPbkdf2(senha, sal, iteracoes);
            return MessageDigest.isEqual(hashEsperado, hashInformado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] gerarPbkdf2(char[] senha, byte[] sal, int iteracoes) {
        try {
            PBEKeySpec especificacao = new PBEKeySpec(senha, sal, iteracoes, TAMANHO_CHAVE);
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return fabrica.generateSecret(especificacao).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de senha indisponível.", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Não foi possível gerar o hash da senha.", e);
        }
    }

    private static String paraHexadecimal(byte[] dados) {
        StringBuilder texto = new StringBuilder(dados.length * 2);
        for (byte dado : dados) {
            texto.append(String.format("%02x", dado & 0xff));
        }
        return texto.toString();
    }

    private static byte[] deHexadecimal(String texto) {
        if (texto.length() % 2 != 0) {
            throw new IllegalArgumentException("Hexadecimal inválido");
        }
        byte[] dados = new byte[texto.length() / 2];
        for (int i = 0; i < texto.length(); i += 2) {
            dados[i / 2] = (byte) Integer.parseInt(texto.substring(i, i + 2), 16);
        }
        return dados;
    }
}
