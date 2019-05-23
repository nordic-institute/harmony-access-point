/**
 * A helper class that's a bit nicer when applying multiple mixins
 *
 * @since 4.1
 */

let mix = (superclass: { new(...args): any }) => new MixinBuilder(superclass);

class MixinBuilder {
  constructor(public superclass) {
    this.superclass = superclass;
  }

  with(...mixins): { new(...args): any } {
    return mixins.reduce((c, mixin) => mixin(c), this.superclass);
  }
}

export default mix;


