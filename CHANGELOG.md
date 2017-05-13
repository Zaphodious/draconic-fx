# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]
### Changed
- Fixed a bug where nested run-now calls would hang the JavaFX Application Thread. Turns out that promises don't really stack all that well!

## [0.4.1]
### Changed
- Added draconic.fx.cells, which contains functions to help add custom cells to views that use them. Proxies FTW!
- Added several new assignment functions
- Specced defcontroller, to try and help avoid errors.
- Added a few helper functions to fx.assignment. Something prevents set!-defaults from working properly, so some things like setting ID (which is identical across all nodes) now go into their own functions.
- Added a resettable fn to fx.ml, so that getting a resource via string is a little less error-prone

## [0.3.0]
### Changed
- Moved example to defn
- Switched over to clojure 1.9.0-alpha14
- Added a convenience macro to make creating controller functions easier.

## [0.2.0]
### Changed
- Changed "draconic.fx.parent" namespace to "draconic.fx.assignment"
- Added support for multiple set and get operations, instead of just for children.
- To that end, changed names and signatures. Now you call draconic.fx.assignment/try-get and try-set!, passing in the target node and a keyword which lets the fn know which operation to perform.
- Added multimethods get-typed, get-defaults, set-typed, and set-defaults. try-x call x-typed, which falls through to x-defaults if no type-specific information is found. x-defaults dispatches on :op-keyword, and x-typed on [:op-keyword, "type ***as a string***"]. Why as a string? Because JavaFX doesn't let me resolve classes sometimes.

## [0.1.0] - 2016-12-04
### Changed
- Initial project and API released

[Unreleased]: https://github.com/Zaphodious/draconic/compare/0.3.0...HEAD
[0.3.0]: https://github.com/Zaphodious/draconic/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/Zaphodious/draconic/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/Zaphodious/draconic/compare/0.1.0...0.1.0
