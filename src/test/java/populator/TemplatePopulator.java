package populator;

import com.carebridge.dao.impl.TemplateDAO;
import com.carebridge.entities.Field;
import com.carebridge.entities.Template;
import com.carebridge.enums.FieldType;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.List;

public class TemplatePopulator {

    // store created test data
    private static final List<Template> data = new ArrayList<>();

    public static void populate() {

        TemplateDAO templateDAO = TemplateDAO.getInstance();

        data.clear(); // avoid duplicates between tests

        // template 1
        Template t1 = new Template();
        t1.setTitle("Template One");

        t1.addField(
                Field.builder().
                        title("Name").
                        fieldType(FieldType.TEXTFIELD)
                        .build()
        );

        t1.addField(
            Field.builder().
                    title("Age").
                    fieldType(FieldType.NUMBERFIELD)
                    .build()
        );


        // template 2
        Template t2 = new Template();
        t2.setTitle("Template Two");

        Field f3 = new Field();
        f3.setTitle("Active");
        f3.setFieldType(FieldType.CHECKBOX);
        f3.setTemplate(t2);

        t2.setFields(List.of(f3));

        // Persist
        templateDAO.create(t1);
        templateDAO.create(t2);

        // store references AFTER persist (IDs assigned)
        data.add(t1);
        data.add(t2);
    }

    public static List<Template> fetch() {
        return data;
    }
}