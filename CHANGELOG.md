# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed

## [0.2.0]
### Changed
- Changed "draconic.fx.parent" namespace to "draconic.fx.assignment"
- Added support for multiple set and get operations, instead of just for children.
- To that end, changed names and signatures. Now you call draconic.fx.assignment/try-get and try-set!, passing in the target node and a keyword which lets the fn know which operation to perform.
- Added multimethods get-typed, get-defaults, set-typed, and set-defaults. try-x call x-typed, which falls through to x-defaults if no type-specific information is found. x-defaults dispatches on :op-keyword, and x-typed on [:op-keyword, "type ***as a string***"]. Why as a string? Because JavaFX doesn't let me resolve classes sometimes.

## [0.1.0] - 2016-12-04
### Changed
- Initial project and API released

[Unreleased]: https://github.com/Zaphodious/draconic/compare/0.2.0...HEAD
[0.0.2]: https://github.com/Zaphodious/draconic/compare/0.1.0...0.2.0
[0.0.1]: https://github.com/Zaphodious/draconic/compare/0.1.0...0.1.0
