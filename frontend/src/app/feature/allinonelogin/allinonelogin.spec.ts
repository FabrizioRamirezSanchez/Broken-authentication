import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Allinonelogin } from './allinonelogin';

describe('Allinonelogin', () => {
  let component: Allinonelogin;
  let fixture: ComponentFixture<Allinonelogin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Allinonelogin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Allinonelogin);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
