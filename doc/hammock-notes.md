# Notes from the Hammock
These notes are thought-stores, from my time as close to the hammock as I can get.


## Generating a UI from a spec
To create a Ui element from a spec, first see if the primary element is a sequence element or a scalar predicate. If seq, act recursively until a predicate is found, wrapping the predicate elements in the appropriate kind of Ui container derived from a UI template (with controls for adding and removing if the seq is open-ended), potentially having an "edit" button that launches another Ui window if the spec is sufficiently complex. If a predicate, call the multimethod to get a Ui element template and then alter it to fit the requirements. 

When looking for the scalar predicate, treat spec/and as basically "look at the next one" right off the bat, and treat spec/or as "make an element for both, and make them switchable". 

The UI template usually comes from an FXML file, with a label element and a control element having standard initial IDs of "specLabel" and "specControl" respectively. Its output conforms to what is used by draconic.fx.ml in order to be usable as a Ui node source by that part of the library, with the slight modification that rather then return the unadorned nodes mapped under their ID strings, instead the map is of the base ID to a map of {:label the-label :control the-control :spec the-spec}

There will be functions (that call multimethods, or might even be multimethods?) which, given a spec-generated UI map, will set! and get the state of the element to and from a spec-conformed value and return them in a vector [name-of-some-kind? thing-what-got-returned].

Possibility- The element that is generated could have a change listener registered, that sends data (when conformed and valid) either through an atom or straight into a channel. Possibly into an atom? If into an atom, the primary method of getting and setting would be via the atom, which has side-effect-producing listeners itself that change the state of the UI components. Potentially, there is a function that adds a channel to an atom?

## Repl-inspired Ui design
   The REPL has three major parts to it, which are shared by all useful guis. First, there's the input box. This is a form with one text box/area and no label, where the user relies on statefulness in order to compose their request. This is equivalent to a function signature. Second is the enter key or (maybe) a button that causes the evaluation of the form in the input box. This is an action starter, which exist to let the system know when input is ready to be processed. This is equivalent to calling a function. Third is the REPL readout, a read-only view of what the REPL has processed up until now. This is equivalent to the return value of a function.
   Every Ui has these properties, even if sometimes a single widget is used for more then one purpose
   The tricky part of Ui design is the form bits
   Hard to conceptualize sometimes, frequently changing, and needs to be synced up to changes in the data
   So, why not do what we always do when confronted with a complicated problem- use an abstraction!
   Note that these types aren't types of Ui widgets, as most widgets are usable for all three. This describes types of use
   The output is an outward boundary in our system's design. What is presented must be understandable by the user, but making sure of this is as simple as transforming data to human-readable text.
   The action-provoker needs little ceremony, as it's a relatively dumb control that exists merely to tell the system which subroutine to run. This might not be explicit in the UI, as we will see. The main thing to watch out fkr in these is that the actions invoked be clear, distinct between different invokers, and consistent given the inputs.
   The main area that needs our concern is the input gatherer
   This is where harmful statefulness lies
   But it's also the most important part of the ui
   Every other thing in the UI, and in the program, is subservient to input given here
   And here, we are thinking wrongly
   The state used in these elements is insignificant to us in our programs. It it best abstracted away as an implementation detail much like transients are when working with seqs
   The most useful way to think about it is as an atom. Indeed, changes that the user makes are very much atomic, in that you can't take an intermediate state and do anything useful with it.
   While the user is composing their inputs, however, it is useful to inform them when they enter something that isn't within the parameters given by the system. This is like instrumenting or giving preconditions to a function.