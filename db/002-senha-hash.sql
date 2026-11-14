-- ==============================================================================
-- NEXTBUS - coluna de senha da API
--
-- Rode este arquivo no SQL Developer depois de 001-tabelas-e-carga.sql.
-- No SQL Developer use F5 (Run Script).
-- O script das tabelas nao e alterado. Esta coluna fica a cargo da API,
-- como o comentario daquele script ja dizia.
--
-- Senha de teste dos usuarios da carga (id 1 a 5): 123456
-- Marina Costa (cidada) e Ricardo Nunes (gestor) entram com essa senha.
-- O valor gravado e o hash BCrypt, o mesmo que o Spring Security confere.
-- Usuarios criados pela API (id a partir de 100) nao sao alterados.
-- ==============================================================================

SET DEFINE OFF;

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE USUARIO ADD (senha_hash VARCHAR2(100 CHAR))';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1430 THEN
            RAISE;
        END IF;
END;
/

UPDATE USUARIO
   SET senha_hash = '$2b$10$DI46IVe1R2XQznh7UmQE3OY7LdBG7E3e23SxG1HfF4P1xVqfGNG9a'
 WHERE id_usuario < 100;

COMMIT;

BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE USUARIO MODIFY (senha_hash VARCHAR2(100 CHAR) NOT NULL)';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -1442 THEN
            RAISE;
        END IF;
END;
/
