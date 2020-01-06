import { Directive, ElementRef, Renderer2, HostListener, Input, OnInit } from '@angular/core';
import { NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Directive({
  selector: '[input-debounce]',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: InputDebounceBehaviourDirective,
    multi: true,
  }],
})
export class InputDebounceBehaviourDirective implements ControlValueAccessor, OnInit {

  onChange: Function;
  onTouched: Function;

  @Input('jrvDebounceControl') debounceTime: number;

  private _timer = null;

  constructor(private _elementRef: ElementRef, private _renderer: Renderer2,) {

  }

  ngOnInit() {
    if (typeof this.debounceTime !== 'number') {
      this.debounceTime = 2000; // default debounce to 2000ms
    }
  }

  @HostListener('input', ['$event.target.value'])
  input(value: string) {
    this.onChange(value);
  }

  writeValue(value: string) {
    const element = this._elementRef.nativeElement;
    this._renderer.setProperty(element, 'value', value);
  }

  registerOnChange(fn: Function) {
    this.onChange = this._debounce(fn);
  }

  registerOnTouched(fn: Function) {
    this.onTouched = this._debounce(fn);
  }

  // private
  private _debounce(fn: Function) {
    return (...args: any[]) => {
      if (this._timer) {
        clearTimeout(this._timer);
      }
      this._timer = setTimeout(() => {
        fn(...args);
        this._timer = null;
      }, this.debounceTime);
    }
  }
}
