/**
 * Base class for components that display a list of items that can be ordered and paged
 * It is an embryo; more common functionality will be added in time
 *
 * @since 4.1
 */

let mix = (superclass: { new(): any }) => new MixinBuilder(superclass);

class MixinBuilder {
  constructor(public superclass) {
    this.superclass = superclass;
  }

  with(...mixins): { new(): any } {
    return mixins.reduce((c, mixin) => mixin(c), this.superclass);
  }
}

export default mix;


