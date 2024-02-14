# IB Computer Science Internal Assessment Product

## Note-taking application

### Supported formulas:
- `/cpy{content}`: adds a button that allows `content` to be copied to clipboard
- `/lnk{url}`: converts `url` to hyperlink that directs user to the specified url on default web browser
- `/hlt{content}`: highlights `content` in `#FFF200`
- `/und{content}`: underlines `content`
- `/bld{content}`: emboldens `content`

### Other features:
- Editing sections
- Archiving sections
- Authentication when accessing/editing/archiving locked sections
- Auto-saving feature
- Auto-completing brackets for parenthesis (), braces {}, and brackets []

Utilizes Java Swing for GUI and `sqlite3` for data management.

Before running, change file path of `SecretFile.key` in `Authentication.java` (lines `168` and `236`) as required.

Still in early development stages.
