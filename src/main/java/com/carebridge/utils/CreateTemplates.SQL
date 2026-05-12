INSERT INTO public.template(
    title, is_usable)
VALUES
    ('test1', TRUE),
    ('test2', FALSE),
    ('test3', TRUE);

INSERT INTO public.field(
    type , template_id, title)
VALUES
    (0, 1, 'Textbox'),
    (1, 1, 'Checkbox'),
    (2, 1, 'Numberbox');