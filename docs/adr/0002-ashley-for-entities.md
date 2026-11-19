# Ashley entity component system for entities

Entities were a class tree: `Entity`, then `Mob`, then one subclass per enemy
type, with movement, AI, health, and shooting inherited together. Arena play
needs enemies that mix behaviors freely and upgrades that change the player's
abilities, which a class tree handles with ever more subclasses. Entities move
to Ashley, libGDX's entity component system: each behavior is a component and
each rule is a system, and an enemy type is a set of components.

## Considered options

- **Keep the tree and extract behaviors into parts that a mob owns**: smaller
  change, but each new combination still needs a class, and systems such as
  mob separation would loop over the tree by hand.
