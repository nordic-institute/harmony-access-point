/**
 * Base class for list components;
 * empty now but common functionality will be added in time
 *
 * @since 4.1
 */
export interface Constructable {
  new (...args);
}

function ConstructableDecorator(constructor: Constructable) {
}

@ConstructableDecorator
export default class BaseListComponent{
  constructor() {
  }
};



