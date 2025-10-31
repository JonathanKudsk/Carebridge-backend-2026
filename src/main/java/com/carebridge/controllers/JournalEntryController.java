import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JournalEntryController {

    private final JournalEntryService journalEntryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JournalEntryController(JournalEntryService service) {
        this.journalEntryService = service;
    }

    // POST /api/journalentries
    public void createJournalEntry(Context ctx) {
        try {
            // 1. Autentificering
            User user = ctx.attribute("user");
            if (user == null) {
                //401 response
                ctx.status(HttpStatus.UNAUTHORIZED);
                return;
            }

            // 2. Autorisation (kun ADMIN eller CARE_WORKER)
            if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.CARE_WORKER) {
                //403 response
                ctx.status(HttpStatus.FORBIDDEN);
                return;
            }

            // 3. Udpak multipart
            String metadataJson = ctx.formParam("metadata");
            if (metadataJson == null || metadataJson.isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).result("Missing 'metadata' part");
                return;
            }

            JournalEntryDTO dto = objectMapper.readValue(metadataJson, JournalEntryDTO.class);
            List<UploadedFile> uploadedFiles = ctx.uploadedFiles("files"); // "files" = feltets navn i multipart-formen

            // 4. Valider DTO (server-side)
            ValidationResult validation = validate(dto);
            if (!validation.isValid()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(validation.getErrors());
                return;
            }

            // 5. Kald service
            JournalEntryDTO created = journalEntryService.create(dto, uploadedFiles, user);

            // 6. Returnér 201 Created + JSON
            ctx.status(HttpStatus.CREATED).json(created);

        } catch (EntityNotFoundException e) {
            ctx.status(HttpStatus.NOT_FOUND).result(e.getMessage());
        } catch (InvalidFileException e) {
            ctx.status(HttpStatus.BAD_REQUEST).result(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log evt. ordentligt i dit projekt
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("Unexpected error: " + e.getMessage());
        }
    }

    // Simpel DTO-validering
    private ValidationResult validate(JournalEntryDTO dto) {
        ValidationResult result = new ValidationResult();

        if (dto.getTitle() == null || dto.getTitle().isBlank())
            result.addError("title", "Title is required");
        if (dto.getContent() == null || dto.getContent().isBlank())
            result.addError("content", "Content is required");
        if (dto.getJournalId() == null)
            result.addError("journalId", "Journal reference is required");
        if (dto.getType() == null)
            result.addError("type", "Journal entry type is required");
        if (dto.getRisk() == null)
            result.addError("risk", "Risk level is required");

        return result;
    }
}
