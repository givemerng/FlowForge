import { NavLink } from 'react-router-dom';
import { Search, Bell, HelpCircle, Moon } from 'lucide-react';

const Header = () => {
  return (
    <header className="flex justify-between items-center w-full px-lg py-sm bg-surface border-b border-outline-variant docked full-width top-0 sticky z-40 hidden md:flex">
      <nav className="flex gap-lg">
        <NavLink to="/dashboard" className={({ isActive }) => `font-label-md text-label-md pb-sm transition-colors block border-b-2 ${isActive ? 'text-primary border-primary' : 'text-on-surface-variant border-transparent hover:text-on-surface'}`}>Overview</NavLink>
        <NavLink to="/projects/1/board" className={({ isActive }) => `font-label-md text-label-md pb-sm transition-colors block border-b-2 ${isActive ? 'text-primary border-primary' : 'text-on-surface-variant border-transparent hover:text-on-surface'}`}>Board</NavLink>
        <NavLink to="/my-tasks" className={({ isActive }) => `font-label-md text-label-md pb-sm transition-colors block border-b-2 ${isActive ? 'text-primary border-primary' : 'text-on-surface-variant border-transparent hover:text-on-surface'}`}>Tasks</NavLink>
        <NavLink to="/analytics" className={({ isActive }) => `font-label-md text-label-md pb-sm transition-colors block border-b-2 ${isActive ? 'text-primary border-primary' : 'text-on-surface-variant border-transparent hover:text-on-surface'}`}>Analytics</NavLink>
        <NavLink to="/insights" className={({ isActive }) => `font-label-md text-label-md pb-sm transition-colors block border-b-2 ${isActive ? 'text-primary border-primary' : 'text-on-surface-variant border-transparent hover:text-on-surface'}`}>Activity</NavLink>
      </nav>
      
      <div className="flex items-center gap-md">
        <div className="relative hidden lg:block">
          <Search className="absolute left-sm top-1/2 -translate-y-1/2 text-outline" size={18} />
          <input 
            className="pl-[32px] pr-sm py-[6px] border border-outline-variant rounded bg-surface focus:border-primary focus:ring-1 focus:ring-primary/20 font-body-sm text-body-sm w-48 transition-all outline-none" 
            placeholder="Search..." 
            type="text" 
          />
        </div>
        
        <button className="text-on-surface-variant hover:text-primary transition-colors p-xs rounded hover:bg-surface-container">
          <Bell size={20} />
        </button>
        <button className="text-on-surface-variant hover:text-primary transition-colors p-xs rounded hover:bg-surface-container">
          <HelpCircle size={20} />
        </button>
        <button className="text-on-surface-variant hover:text-primary transition-colors p-xs rounded hover:bg-surface-container">
          <Moon size={20} />
        </button>
        
        <img 
          alt="User Avatar" 
          className="w-8 h-8 rounded-full border border-outline-variant object-cover ml-sm" 
          src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80" 
        />
      </div>
    </header>
  );
};

export default Header;
