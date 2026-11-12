# Writing guidelines

These rules apply to code comments, KDoc, the docs site (`docs/ghpages`), module READMEs,
the CHANGELOG, commit messages and PR descriptions.

## Wording

- State the component, the action and the condition: "the container requires a filter",
  not "the container wants a filter".
- Software does not intend, travel or pay. Re-read each use of: wants, asks, reaches,
  arrives, lands, survives, holds, carries, spends, costs, earns, remembers. Replace the
  word with the literal verb: requires, calls, imports, contains, declares, stores, is
  restored, takes.
- State the actor. "Nothing" and "nobody" are not subjects: "no plugin receives the
  event", not "nothing sees the event".
- Repeat the noun. "It", "this" and "they" are allowed only when the noun is the subject
  of the same sentence and no other noun could be meant.
- A counting word keeps its noun: "each destination", "all three containers".
- Each sentence must be clear when quoted alone.

## Docs site and READMEs

A docs page tells a developer what an API does and how to call the API.

- Short and literal. Use fewer words than feels natural.
- Name the API in the sentence: "`NavigationDisplay` renders the container's backstack",
  not "it renders the backstack".
- State what the API does, then the condition or limit. No benefit clauses ("so you
  never have to…"), no reassurance ("that's it", "simply", "just"), no sales language
  ("powerful", "seamless", "first-class").
- No clever headings. A heading names the API or the task: "Returning a result from an
  Activity".
- No questions as headings or lead-ins, and no "let's".
- A claim about behaviour must be true of the current code. Check the source before
  writing the claim. Leave out a claim that cannot be checked.
- One statement of a fact per page. Link to the page that owns the fact instead of
  repeating the fact.
- Describe the API as the API is now. History belongs in the CHANGELOG and the migration
  guide.
- Kotlin samples are bound snippets (see `CLAUDE.md`). Comments inside a sample follow
  the comment rules below: a comment states what the reader cannot see in the sample.

## Code comments

A comment must say something the code cannot. If a competent reader can recover the
point from the names, types and structure, delete the comment. The default is no
comment. Before writing a comment, try a clearer name, a `require(...)` whose message
states the rule, or a restructured flow. A comment that remains is one line, two at most.

A comment addresses the next reader of the file, never the reviewer of the change. Why
the diff exists, what the diff replaces and which issue requested the change belong in
the commit message or PR description.

### Comments to write

- **Invariants the type system can't express.** An ordering the code relies on but does
  not enforce, a string that must stay identical to a value defined elsewhere.
- **Deliberate decisions that look wrong.** Where a maintainer would "fix" correct code,
  say why the code is correct.
- **Root-caused workarounds.** State the root cause and the upstream issue, so the
  workaround can be deleted when the root cause is fixed.
- **Contract KDoc on public API.** Enro is a library in explicit API mode, and Dokka
  publishes the KDoc. Public KDoc states contract: edge-case behaviour, ordering,
  nullability, threading, what happens on failure, platform differences. KDoc is not
  mandatory: `/** The instance id. */ val id` is worse than no KDoc. KDoc does not
  restate the name, describe the implementation, or tell the history of the API.

### Comments not to write

- **Next-line narration** — `// open the destination` above `navigation.open(key)`.
- **Name restatement** — KDoc or comments that paraphrase the identifier or type.
- **Change narration** — "now uses X", "previously…", "no longer", "per review feedback".
- **Justification to the reviewer** — arguing that the code is correct instead of stating
  the invariant that makes the code correct.
- **Rationale essays** — a paragraph of design reasoning above a declaration. State the
  constraint in one line, or put the reasoning in the commit message.
- **Section banners** — `// ---- helpers ----`. Split a file that needs banners.
- **Commented-out code** — delete the code; git history keeps the deleted lines.

### One canonical home

State an invariant once, at the declaration. Repeat the invariant only where a
maintainer could violate the invariant without seeing the declaration, and keep the
repeat to one line.

### Tests

Test names state what each test checks. Comment only where an assertion is ambiguous,
above all when asserting absence.

### Defaults

When in doubt, write no comment. Do not raise the comment density of the file being
edited.

## Recipes

A recipe file is teaching code, so a recipe has more comments than library code. A
recipe comment states what the reader should notice about the Enro API at that line. A
recipe comment does not narrate the next line and does not describe Compose layout code.

## Before handing off a change

Re-read every comment and every docs sentence the diff adds, including work delegated to
subagents. Delete each comment that narrates the change, argues with the reviewer, or
restates a name or type. Rewrite each sentence that breaks the wording rules.
