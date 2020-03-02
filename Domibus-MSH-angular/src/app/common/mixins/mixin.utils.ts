import {Constructable} from './base-list.component';

/**
 * @author Ion Perpegel
 * @since 4.1
 *
 * A helper class that's a bit nicer when applying multiple mixins
 * */

let mix = (superclass: Constructable) => new MixinBuilder(superclass);

class MixinBuilder {
  constructor(public superclass) {
    this.superclass = superclass;
  }

  with(...mixins):Constructable {
    return mixins.reduce((c, mixin) => mixin(c), this.superclass);
  }
}

export default mix;


