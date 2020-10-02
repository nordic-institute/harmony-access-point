import {Directive, ElementRef, HostListener, Input, OnInit, Renderer2} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';

/**
 * @author Ion Perpegel
 * @since 4.2
 *
 * Behavioural directive for input fields to control when the validation message will be shown
 * It displays it on blur and by typing, after 2 seconds. When clear value, it does not display the error message
 */
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

  @Input('jrvDebounceControl')
  debounceTime: number;

  private _timer = null;

  private setValueFn: Function;
  private lastValue: string;

  constructor(private _elementRef: ElementRef, private _renderer: Renderer2) {
  }

  ngOnInit() {
    if (typeof this.debounceTime !== 'number') {
      this.debounceTime = 2000; // default debounce to 2sec
    }
  }

  @HostListener('input', ['$event.target.value'])
  input(value: string) {
    this.lastValue = value;
    if (value) {
      this.onChange(value);
    } else {
      this.setValueFn(value);
    }
  }

  @HostListener('blur')
  onBlur() {
    if (this.setValueFn) {
      const element = this._elementRef.nativeElement;
      if (element && element.value != this.lastValue) {
        this.setValueFn(this.lastValue);
      }
    }
  }

  writeValue(value: string) {
    this.lastValue = value;
    const element = this._elementRef.nativeElement;
    this._renderer.setProperty(element, 'value', (value == null ? '' : value));
  }

  registerOnChange(fn: Function) {
    this.onChange = this._debounce(fn);
    this.setValueFn = fn;
  }

  registerOnTouched(fn: Function) {
    this.onTouched = this._debounce(fn);
  }

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
