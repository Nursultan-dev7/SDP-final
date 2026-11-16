# Team and contribution evidence

Repository: <https://github.com/Nursultan-dev7/SDP-final>.

| Student | GitHub | Implemented patterns and commit/PR evidence |
| --- | --- | --- |
| Ташкенбаев Нурсултан | Nursultan-dev7 | Confirm two implemented patterns and their actual commits/PRs |
| Султанкул Шынгыс | chinusoid | Confirm two implemented patterns and their actual commits/PRs |
| Лопаткин Артем | ArtemLopatkin | Confirm two implemented patterns and their actual commits/PRs |

The original repository commit `85494f7` is retained, with its author Nursultan-dev7. This branch records the review and hierarchy refactor under the configured chinusoid identity. Neither an import commit nor this refactor proves that all three students individually implemented at least two patterns. Do not rewrite authorship or manufacture history to make that claim.

Each member should make their own substantive implementation/test changes, run `test.cmd` or `sh test.sh`, commit under their own Git identity and push a branch. Record the implemented patterns and real commit/PR links in the table and PDF. Confirm actual work with the team before claiming ownership.

A balanced **proposal, not verified completed authorship**, is:

| Proposed owner | Two patterns | Work to demonstrate |
| --- | --- | --- |
| Nursultan-dev7 | Builder + Strategy | Transaction invariants, fee algorithms and boundary tests |
| chinusoid | Decorator + Facade | Authorization and integrated use cases |
| ArtemLopatkin | Adapter + Observer | Unit/status conversion and confirmation events |

Merely changing comments or names does not demonstrate implementing two patterns. The team must be able to explain the complete workflow and their individual implementation decisions.

```sh
git log --all --format="%h %an <%ae> %s"
git shortlog -sne --all
git remote -v
```

Check that all real contributions are pushed and visible to the lecturer. Every student must separately submit the PDF and source ZIP before the deadline announced in Moodle/course materials. No calendar deadline was present in the supplied PDF.
