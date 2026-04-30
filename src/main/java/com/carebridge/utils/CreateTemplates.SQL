INSERT INTO public.template(
    title)
VALUES
    ('test1'),
    ('test2'),
    ('test3');

INSERT INTO public.field(
    type , template_id, title)
VALUES
    (0, 1, 'Textbox'),
    (1, 1, 'Checkbox'),
    (2, 1, 'Numberbox');