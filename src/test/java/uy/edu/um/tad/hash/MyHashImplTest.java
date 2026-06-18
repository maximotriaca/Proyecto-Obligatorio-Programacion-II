package uy.edu.um.tad.hash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de MyHashImpl")
class MyHashImplTest {

    private MyHashImpl<Integer, String> hash;

    @BeforeEach
    void setUp() {
        hash = new MyHashImpl<>();
    }

    // =========================================================
    // size()
    // =========================================================
    @Nested
    @DisplayName("size()")
    class SizeTests {

        @Test
        @DisplayName("Hash recién creado tiene tamaño 0")
        void emptyHash_sizeIsZero() {
            assertEquals(0, hash.size());
        }

        @Test
        @DisplayName("Hash con un elemento tiene tamaño 1")
        void oneElement_sizeIsOne() {
            hash.put(1, "alfa");
            assertEquals(1, hash.size());
        }

        @Test
        @DisplayName("Hash con varios elementos reporta tamaño correcto")
        void multipleElements_sizeIsCorrect() {
            hash.put(1, "alfa");
            hash.put(2, "beta");
            hash.put(3, "gamma");
            assertEquals(3, hash.size());
        }
    }

    // =========================================================
    // put() y get()
    // =========================================================
    @Nested
    @DisplayName("put() y get()")
    class PutAndGetTests {

        @Test
        @DisplayName("get() en hash vacío retorna null")
        void emptyHash_getReturnsNull() {
            assertNull(hash.get(1));
        }

        @Test
        @DisplayName("put() y get() con un elemento retorna el valor correcto")
        void oneElement_getReturnsCorrectValue() {
            hash.put(1, "alfa");
            assertEquals("alfa", hash.get(1));
        }

        @Test
        @DisplayName("put() con clave existente sobreescribe el valor")
        void sameKey_putOverwritesValue() {
            hash.put(1, "alfa");
            hash.put(1, "beta");
            assertEquals("beta", hash.get(1));
            assertEquals(1, hash.size());
        }

        @Test
        @DisplayName("get() con clave inexistente retorna null")
        void missingKey_getReturnsNull() {
            hash.put(1, "alfa");
            assertNull(hash.get(99));
        }

        @Test
        @DisplayName("put() y get() con varios elementos retorna cada valor correctamente")
        void multipleElements_getReturnsCorrectValues() {
            hash.put(1, "alfa");
            hash.put(2, "beta");
            hash.put(3, "gamma");
            assertEquals("alfa", hash.get(1));
            assertEquals("beta", hash.get(2));
            assertEquals("gamma", hash.get(3));
        }
    }

    // =========================================================
    // contains()
    // =========================================================
    @Nested
    @DisplayName("contains()")
    class ContainsTests {

        @Test
        @DisplayName("contains() en hash vacío retorna false")
        void emptyHash_containsReturnsFalse() {
            assertFalse(hash.contains(1));
        }

        @Test
        @DisplayName("contains() retorna true para clave existente")
        void existingKey_containsReturnsTrue() {
            hash.put(1, "alfa");
            assertTrue(hash.contains(1));
        }

        @Test
        @DisplayName("contains() retorna false para clave inexistente")
        void missingKey_containsReturnsFalse() {
            hash.put(1, "alfa");
            assertFalse(hash.contains(99));
        }
    }

    // =========================================================
    // remove()
    // =========================================================
    @Nested
    @DisplayName("remove()")
    class RemoveTests {

        @Test
        @DisplayName("remove() en hash vacío no lanza excepción")
        void emptyHash_removeDoesNotThrow() {
            assertDoesNotThrow(() -> hash.remove(1));
        }

        @Test
        @DisplayName("remove() el único elemento deja el hash vacío")
        void oneElement_removeLeaveHashEmpty() {
            hash.put(1, "alfa");
            hash.remove(1);
            assertEquals(0, hash.size());
            assertFalse(hash.contains(1));
        }

        @Test
        @DisplayName("remove() un elemento de varios lo elimina correctamente")
        void multipleElements_removeDeletesCorrectElement() {
            hash.put(1, "alfa");
            hash.put(2, "beta");
            hash.put(3, "gamma");
            hash.remove(2);
            assertEquals(2, hash.size());
            assertFalse(hash.contains(2));
            assertTrue(hash.contains(1));
            assertTrue(hash.contains(3));
        }

        @Test
        @DisplayName("remove() con clave inexistente no modifica el hash")
        void missingKey_removeDoesNothing() {
            hash.put(1, "alfa");
            hash.remove(99);
            assertEquals(1, hash.size());
        }
    }

    // =========================================================
    // keys() y values()
    // =========================================================
    @Nested
    @DisplayName("keys() y values()")
    class KeysAndValuesTests {

        @Test
        @DisplayName("keys() en hash vacío retorna lista vacía")
        void emptyHash_keysReturnsEmptyList() {
            assertEquals(0, hash.keys().size());
        }

        @Test
        @DisplayName("values() en hash vacío retorna lista vacía")
        void emptyHash_valuesReturnsEmptyList() {
            assertEquals(0, hash.values().size());
        }

        @Test
        @DisplayName("keys() retorna todas las claves")
        void multipleElements_keysReturnsAllKeys() {
            hash.put(1, "alfa");
            hash.put(2, "beta");
            hash.put(3, "gamma");
            assertEquals(3, hash.keys().size());
            assertTrue(hash.keys().contains(1));
            assertTrue(hash.keys().contains(2));
            assertTrue(hash.keys().contains(3));
        }

        @Test
        @DisplayName("values() retorna todos los valores")
        void multipleElements_valuesReturnsAllValues() {
            hash.put(1, "alfa");
            hash.put(2, "beta");
            hash.put(3, "gamma");
            assertEquals(3, hash.values().size());
            assertTrue(hash.values().contains("alfa"));
            assertTrue(hash.values().contains("beta"));
            assertTrue(hash.values().contains("gamma"));
        }
    }
}