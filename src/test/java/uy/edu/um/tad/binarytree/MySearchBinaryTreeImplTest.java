package uy.edu.um.tad.binarytree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de MySearchBinaryTreeImpl")
class MySearchBinaryTreeImplTest {

    private MySearchBinaryTreeImpl<Integer, String> tree;

    @BeforeEach
    void setUp() {
        tree = new MySearchBinaryTreeImpl<>();
    }

    // =========================================================
    // isEmpty()
    // =========================================================
    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTests {

        @Test
        @DisplayName("Árbol recién creado debe ser vacío")
        void emptyTree_isEmptyReturnsTrue() {
            assertTrue(tree.isEmpty());
        }

        @Test
        @DisplayName("Árbol con un elemento no debe ser vacío")
        void oneElement_isEmptyReturnsFalse() {
            tree.add(10, "alfa");
            assertFalse(tree.isEmpty());
        }
    }

    // =========================================================
    // add() y find()
    // =========================================================
    @Nested
    @DisplayName("add() y find()")
    class AddAndFindTests {

        @Test
        @DisplayName("find() en árbol vacío retorna null")
        void emptyTree_findReturnsNull() {
            assertNull(tree.find(10));
        }

        @Test
        @DisplayName("add() y find() con un elemento retorna el valor correcto")
        void oneElement_findReturnsCorrectValue() {
            tree.add(10, "alfa");
            assertEquals("alfa", tree.find(10));
        }

        @Test
        @DisplayName("find() con clave inexistente retorna null")
        void missingKey_findReturnsNull() {
            tree.add(10, "alfa");
            assertNull(tree.find(99));
        }

        @Test
        @DisplayName("find() retorna el valor de la raíz correctamente")
        void multipleElements_findReturnsRootValue() {
            tree.add(10, "alfa");
            tree.add(5, "beta");
            tree.add(15, "gamma");
            assertEquals("alfa", tree.find(10));
        }

        @Test
        @DisplayName("add() con clave duplicada la inserta a la izquierda sin modificar la raíz")
        void sameKey_addDoesNotOverwriteRoot() {
            tree.add(10, "alfa");
            tree.add(10, "beta");
            assertEquals("alfa", tree.find(10));
        }
    }

    // =========================================================
    // contains()
    // =========================================================
    @Nested
    @DisplayName("contains()")
    class ContainsTests {

        @Test
        @DisplayName("contains() en árbol vacío retorna false")
        void emptyTree_containsReturnsFalse() {
            assertFalse(tree.contains(10));
        }

        @Test
        @DisplayName("contains() retorna true para la raíz")
        void rootKey_containsReturnsTrue() {
            tree.add(10, "alfa");
            assertTrue(tree.contains(10));
        }

        @Test
        @DisplayName("contains() retorna false para clave inexistente")
        void missingKey_containsReturnsFalse() {
            tree.add(10, "alfa");
            assertFalse(tree.contains(99));
        }

        @Test
        @DisplayName("contains() encuentra la raíz en árbol con varios elementos")
        void multipleElements_containsFindsRoot() {
            tree.add(10, "alfa");
            tree.add(5, "beta");
            tree.add(15, "gamma");
            assertTrue(tree.contains(10));
        }
    }

    // =========================================================
    // remove()
    // =========================================================
    @Nested
    @DisplayName("remove()")
    class RemoveTests {

        @Test
        @DisplayName("remove() en árbol vacío no lanza excepción")
        void emptyTree_removeDoesNotThrow() {
            assertDoesNotThrow(() -> tree.remove(10));
        }

        @Test
        @DisplayName("remove() el único elemento deja el árbol vacío")
        void oneElement_removeLeavesTreeEmpty() {
            tree.add(10, "alfa");
            tree.remove(10);
            assertTrue(tree.isEmpty());
            assertNull(tree.find(10));
        }

        @Test
        @DisplayName("remove() la raíz en árbol con hijo derecho actualiza la raíz")
        void rootWithRightChild_removeUpdatesRoot() {
            tree.add(10, "alfa");
            tree.add(15, "gamma");
            tree.remove(10);
            assertFalse(tree.isEmpty());
            assertTrue(tree.contains(15));
        }

        @Test
        @DisplayName("remove() nodo con dos hijos mantiene el resto del árbol")
        void multipleElements_removeNodeWithTwoChildren() {
            tree.add(10, "alfa");
            tree.add(15, "gamma");
            tree.add(20, "epsilon");
            tree.add(12, "delta");
            tree.remove(15);
            assertFalse(tree.isEmpty());
            assertEquals("alfa", tree.find(10)); // la raíz sigue existiendo
        }
    }

    // =========================================================
    // inOrder() e inOrderValues()
    // =========================================================
    @Nested
    @DisplayName("inOrder() e inOrderValues()")
    class InOrderTests {

        @Test
        @DisplayName("inOrder() en árbol vacío retorna lista vacía")
        void emptyTree_inOrderReturnsEmptyList() {
            assertEquals(0, tree.inOrder().size());
        }

        @Test
        @DisplayName("inOrder() con un elemento retorna lista de un elemento")
        void oneElement_inOrderReturnsOneElement() {
            tree.add(10, "alfa");
            assertEquals(1, tree.inOrder().size());
            assertEquals(10, tree.inOrder().get(0));
        }

        @Test
        @DisplayName("inOrder() retorna las claves en orden ascendente")
        void multipleElements_inOrderReturnsAscendingKeys() {
            tree.add(10, "alfa");
            tree.add(5, "beta");
            tree.add(15, "gamma");

            var keys = tree.inOrder();
            assertEquals(3, keys.size());
            assertEquals(5,  keys.get(0));
            assertEquals(10, keys.get(1));
            assertEquals(15, keys.get(2));
        }

        @Test
        @DisplayName("inOrderValues() retorna los valores en orden de clave ascendente")
        void multipleElements_inOrderValuesReturnsCorrectOrder() {
            tree.add(10, "alfa");
            tree.add(5, "beta");
            tree.add(15, "gamma");

            var values = tree.inOrderValues();
            assertEquals(3, values.size());
            assertEquals("beta",  values.get(0));
            assertEquals("alfa",  values.get(1));
            assertEquals("gamma", values.get(2));
        }
    }
}