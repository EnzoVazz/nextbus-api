-- ==============================================================================
-- NEXTBUS - coluna senha_hash da tabela USUARIO
-- Hash BCrypt da senha de teste 123456 para os usuarios da carga (id menor que 100).
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
