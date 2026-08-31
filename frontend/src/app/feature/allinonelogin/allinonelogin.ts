import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Tab = 'login' | 'reset' | 'register' | 'token';
type Feature = { icon: string; text: string };

type PanelInfo = {
  bg: string; blob: string; text: string; sub: string;
  icon: string; headline: string; desc: string; features: Feature[]; footerIcon: string; footerText: string;
};

@Component({
  selector: 'app-allinonelogin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './allinonelogin.html',
  styleUrl: './allinonelogin.scss',
})
export class Allinonelogin {
  activeTab: Tab = 'login';
  
  // Modales/Visibilidad de contraseñas
  showLoginPassword = false;
  showResetPassword = false;
  showRegisterPassword = false;

  // Modelos de datos para cada formulario
  loginData = {
    username: '',
    password: ''
  };

  resetData = {
    username: '',
    newPassword: ''
  };

  registerData = {
    username: '',
    email: '',
    password: '',
    role: 'user' // Rol por defecto
  };

  tokenData = {
    token: ''
  };

  // Configuración de pestañas
  tabs: { id: Tab; label: string; icon: string }[] = [
    { id: 'login', label: 'Login', icon: 'login' },
    { id: 'reset', label: 'Contraseña', icon: 'lock_reset' },
    { id: 'register', label: 'Nuevo Usuario', icon: 'person_add' },
    { id: 'token', label: 'Token', icon: 'verified_user' },
  ];

  bgGradients: Record<Tab, string> = {
    login: 'linear-gradient(135deg, #fce4ef 0%, #f8d7e8 40%, #ffe8f4 100%)',
    reset: 'linear-gradient(135deg, #f0e4fc 0%, #e8d5f5 40%, #f0e8ff 100%)',
    register: 'linear-gradient(135deg, #d8f5ec 0%, #d5f0e8 40%, #e4f8f0 100%)',
    token: 'linear-gradient(135deg, #d8edfb 0%, #d5e8f8 40%, #e4f2ff 100%)',
  };

  blobs: Record<Tab, { color: string; positions: string[] }> = {
    login: { color: '#f0afd0', positions: ['top-[-60px] left-[-50px]', 'bottom-[-50px] right-[-40px]'] },
    reset: { color: '#c9a8eb', positions: ['top-[-60px] right-[-50px]', 'bottom-[-50px] left-[-40px]'] },
    register: { color: '#8ed9bf', positions: ['top-[-55px] left-[-45px]', 'bottom-[-55px] right-[-45px]'] },
    token: { color: '#8fc8f0', positions: ['top-[-50px] right-[-40px]', 'bottom-[-60px] left-[-50px]'] },
  };

  leftPanels: Record<Tab, PanelInfo> = {
    login: {
      bg: 'linear-gradient(160deg, #fce4ef 0%, #f8c8de 60%, #fad4ea 100%)',
      blob: '#f0afd0', text: '#9d2f60', sub: '#c0607a', icon: 'account_circle',
      headline: '¡Hola de nuevo!\nBienvenido.',
      desc: 'Inicia sesión con tu usuario y contraseña para continuar donde lo dejaste.',
      features: [
        { icon: 'person', text: 'Acceso por usuario' },
        { icon: 'lock', text: 'Cifrado de extremo a extremo' },
        { icon: 'save', text: 'Sesiones guardadas' }
      ],
      footerIcon: 'spa', footerText: 'Seguro, simple y siempre tuyo.',
    },
    reset: {
      bg: 'linear-gradient(160deg, #f0e4fc 0%, #dfc8f7 60%, #e8d8fc 100%)',
      blob: '#c9a8eb', text: '#5a2d9a', sub: '#8050c0', icon: 'lock_reset',
      headline: '¿Olvidaste tu\nclave?',
      desc: 'Ingresa tu usuario y la nueva contraseña para restablecer el acceso a tu cuenta.',
      features: [
        { icon: 'badge', text: 'Validación por usuario' },
        { icon: 'bolt', text: 'Actualización al instante' },
        { icon: 'shield', text: 'Protocolo seguro' }
      ],
      footerIcon: 'auto_awesome', footerText: 'Recupera el control de tu cuenta.',
    },
    register: {
      bg: 'linear-gradient(160deg, #d8f5ec 0%, #b8ecd8 60%, #cdf0e4 100%)',
      blob: '#8ed9bf', text: '#1a6b4a', sub: '#3a8f68', icon: 'person_add',
      headline: 'Crea tu cuenta\nen segundos.',
      desc: 'Regístrate ingresando tu información básica y asigna el rol correspondiente.',
      features: [
        { icon: 'mail', text: 'Registro con Email' },
        { icon: 'admin_panel_settings', text: 'Asignación de Roles' },
        { icon: 'public', text: 'Acceso inmediato' }
      ],
      footerIcon: 'eco', footerText: 'Únete a nuestra plataforma hoy.',
    },
    token: {
      bg: 'linear-gradient(160deg, #d8edfb 0%, #b8d8f5 60%, #cce4fa 100%)',
      blob: '#8fc8f0', text: '#0e4a7a', sub: '#2870a8', icon: 'verified_user',
      headline: 'Autenticación\npor Token.',
      desc: 'Introduce tu token de seguridad para verificar tu acceso de forma inmediata.',
      features: [
        { icon: 'key', text: 'Acceso con Token' },
        { icon: 'timer', text: 'Expiración temporizada' },
        { icon: 'lock_person', text: 'Verificación 2FA / Bearer' }
      ],
      footerIcon: 'cloud', footerText: 'Seguridad avanzada de tokens.',
    },
  };

  get panel() {
    return this.leftPanels[this.activeTab];
  }

  setActive(tab: Tab) {
    this.activeTab = tab;
  }

  // Métodos de envío
  onLogin() {
    console.log('Login:', this.loginData);
  }

  onResetPassword() {
    console.log('Reset Password:', this.resetData);
  }

  onRegister() {
    console.log('Registro:', this.registerData);
  }

  onVerifyToken() {
    console.log('Validación Token:', this.tokenData);
  }
}