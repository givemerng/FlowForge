import { NavLink } from 'react-router-dom';
import { 
  Plus, 
  LayoutDashboard, 
  CheckSquare, 
  FolderKanban, 
  Bell, 
  Users, 
  BarChart3, 
  ShieldAlert, 
  UserCircle, 
  Settings 
} from 'lucide-react';

const Sidebar = () => {
  const navItems = [
    { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
    { name: 'My Tasks', path: '/my-tasks', icon: CheckSquare },
    { name: 'Projects', path: '/projects', icon: FolderKanban },
    { name: 'Notifications', path: '/notifications', icon: Bell },
    { name: 'Workspace', path: '#', icon: Users },
    { name: 'Analytics', path: '/analytics', icon: BarChart3 },
    { name: 'Administration', path: '/monitoring', icon: ShieldAlert },
  ];

  const bottomItems = [
    { name: 'Profile', path: '#', icon: UserCircle },
    { name: 'Settings', path: '/settings', icon: Settings },
  ];

  return (
    <nav className="hidden md:flex flex-col w-60 h-screen fixed left-0 top-0 bg-surface-container-lowest border-r border-outline-variant p-md z-50">
      <div className="mb-xl">
        <h1 className="font-h2 text-h2 font-bold text-primary">FlowForge</h1>
        <p className="font-metadata text-metadata text-on-surface-variant">v2.4.0</p>
      </div>
      
      <button className="w-full bg-primary-container text-on-primary-container font-label-md text-label-md py-sm px-md rounded mb-lg hover:bg-primary-container/90 transition-colors flex items-center justify-center gap-sm">
        <Plus size={18} />
        New Project
      </button>
      
      <ul className="flex flex-col gap-xs flex-1 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <li key={item.name}>
              <NavLink 
                to={item.path}
                className={({ isActive }) => 
                  `flex items-center gap-md px-md py-sm rounded-lg transition-colors font-label-md text-label-md ${
                    isActive 
                    ? 'bg-secondary-container text-on-secondary-container opacity-90' 
                    : 'text-on-surface-variant hover:bg-surface-container-high'
                  }`
                }
              >
                <Icon size={20} />
                {item.name}
              </NavLink>
            </li>
          );
        })}
      </ul>
      
      <div className="mt-auto border-t border-outline-variant pt-sm">
        <ul className="flex flex-col gap-xs">
          {bottomItems.map((item) => {
            const Icon = item.icon;
            return (
              <li key={item.name}>
                <NavLink 
                  to={item.path}
                  className={({ isActive }) => 
                    `flex items-center gap-md px-md py-sm rounded-lg transition-colors font-label-md text-label-md ${
                      isActive 
                      ? 'bg-secondary-container text-on-secondary-container opacity-90' 
                      : 'text-on-surface-variant hover:bg-surface-container-high'
                    }`
                  }
                >
                  <Icon size={20} />
                  {item.name}
                </NavLink>
              </li>
            );
          })}
        </ul>
      </div>
    </nav>
  );
};

export default Sidebar;
