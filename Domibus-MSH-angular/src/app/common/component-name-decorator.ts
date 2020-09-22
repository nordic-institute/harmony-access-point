export function ComponentName(name: string): ClassDecorator {

  return function (constructor: any) {
    Object.defineProperty(constructor.prototype, 'name', {
      get: function () {
        return name;
      }
    });
  }

}
