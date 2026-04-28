package com.carebridge.crud.data.core;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Concrete implementation for testing
    static class TestEntity extends BaseEntity {}

    @Test
    void testId() {
        TestEntity entity = new TestEntity();
        assertNull(entity.getId());
        entity.setId(1L);
        assertEquals(1L, entity.getId());
    }

    @Test
    void testParentAndChildren() {
        TestEntity parent = new TestEntity();
        TestEntity child = new TestEntity();
        
        child.setParent(parent);
        assertEquals(parent, child.getParent());
        child.setParent(null);
        assertNull(child.getParent());

        List<BaseEntity> children = new ArrayList<>();
        children.add(child);
        parent.setChildren(children);
        assertEquals(1, parent.getChildren().size());
        assertEquals(child, parent.getChildren().get(0));
        
        parent.setChildren(null);
        assertNull(parent.getChildren());
    }

    @Test
    void testGetGrandparent() {
        TestEntity grandparent = new TestEntity();
        TestEntity parent = new TestEntity();
        TestEntity child = new TestEntity();

        // Branch 1: No parent
        assertEquals(Optional.empty(), child.getGrandparent());

        // Branch 2: Parent but no grandparent
        child.setParent(parent);
        assertEquals(Optional.empty(), child.getGrandparent());

        // Branch 3: Grandparent exists
        parent.setParent(grandparent);
        assertEquals(Optional.of(grandparent), child.getGrandparent());
    }

    @Test
    void testGetGrandchildren() {
        TestEntity grandparent = new TestEntity();
        TestEntity parent1 = new TestEntity();
        TestEntity parent2 = new TestEntity();
        TestEntity grandchild1 = new TestEntity();
        TestEntity grandchild2 = new TestEntity();
        TestEntity grandchild3 = new TestEntity();

        // Branch 1: No children
        assertTrue(grandparent.getGrandchildren().isEmpty());

        // Branch 2: Children but no grandchildren
        grandparent.getChildren().add(parent1);
        grandparent.getChildren().add(parent2);
        assertTrue(grandparent.getGrandchildren().isEmpty());

        // Branch 3: Grandchildren exist
        parent1.getChildren().add(grandchild1);
        parent1.getChildren().add(grandchild2);
        parent2.getChildren().add(grandchild3);

        List<BaseEntity> grandchildren = grandparent.getGrandchildren();
        assertEquals(3, grandchildren.size());
        assertTrue(grandchildren.contains(grandchild1));
        assertTrue(grandchildren.contains(grandchild2));
        assertTrue(grandchildren.contains(grandchild3));
    }
}
