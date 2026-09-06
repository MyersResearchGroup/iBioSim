export const translations = {
  ar: {
    app_name: 'نظام إدارة المخاطر - تجمع عسير الصحي',
    login: 'تسجيل الدخول', register: 'إنشاء حساب', email: 'البريد الإلكتروني', password: 'كلمة المرور',
    full_name: 'الاسم الكامل', job_title: 'المسمى الوظيفي', login_button: 'دخول', logout: 'تسجيل الخروج',
    dashboard: 'لوحة المعلومات', risk_register: 'سجل المخاطر', users: 'المستخدمون', settings: 'الإعدادات',
    audit_log: 'سجل التدقيق', new_risk: 'إضافة خطر', search: 'بحث...', status: 'الحالة', category: 'المجال الرئيسي',
    residual_level: 'المستوى المتبقي', risk_owner: 'مالك الخطر', risk_code: 'رمز الخطر', risk_statement: 'وصف الخطر',
    likelihood: 'الاحتمالية', impact: 'الأثر', inherent_rating: 'التقييم الكامن', save: 'حفظ', cancel: 'إلغاء',
    edit: 'تعديل', delete: 'حذف', view: 'عرض', total_risks: 'إجمالي المخاطر', open_risks: 'مخاطر مفتوحة',
    top_risks: 'أهم المخاطر', critical_high: 'حرجة/عالية', risks_by_category: 'المخاطر حسب المجال',
    risks_by_level: 'المخاطر حسب المستوى المتبقي', ai_assistant: 'المساعد الذكي', ask_ai: 'اسأل المساعد الذكي',
    profile: 'الملف الشخصي', language: 'اللغة', role: 'الصلاحية', organization: 'الجهة', sector: 'القطاع',
    department: 'الإدارة', root_causes: 'مسببات الخطر', mitigation_plans: 'خطط التخفيف', controls: 'الضوابط الرقابية',
    export_excel: 'تصدير إكسل', export_pdf: 'تصدير PDF', notifications: 'الإشعارات', no_data: 'لا توجد بيانات',
    page: 'صفحة', of: 'من', next: 'التالي', previous: 'السابق', all: 'الكل', identification_date: 'تاريخ التحديد',
    close: 'إغلاق', change_password: 'تغيير كلمة المرور', current_password: 'كلمة المرور الحالية',
    new_password: 'كلمة المرور الجديدة', confirm: 'تأكيد', add_update: 'إضافة تحديث', add_mitigation: 'إضافة خطة تخفيف',
    suggest_mitigations: 'اقترح خطط تخفيف بالذكاء الاصطناعي', loading: 'جاري التحميل...', error_occurred: 'حدث خطأ',
    welcome: 'مرحباً', invalid_credentials: 'بيانات الدخول غير صحيحة', results: 'نتيجة'
  },
  en: {
    app_name: 'Asir Health Cluster - ERM System', login: 'Login', register: 'Register', email: 'Email',
    password: 'Password', full_name: 'Full Name', job_title: 'Job Title', login_button: 'Sign In', logout: 'Logout',
    dashboard: 'Dashboard', risk_register: 'Risk Register', users: 'Users', settings: 'Settings',
    audit_log: 'Audit Log', new_risk: 'New Risk', search: 'Search...', status: 'Status', category: 'Category',
    residual_level: 'Residual Level', risk_owner: 'Risk Owner', risk_code: 'Risk ID', risk_statement: 'Risk Statement',
    likelihood: 'Likelihood', impact: 'Impact', inherent_rating: 'Inherent Rating', save: 'Save', cancel: 'Cancel',
    edit: 'Edit', delete: 'Delete', view: 'View', total_risks: 'Total Risks', open_risks: 'Open Risks',
    top_risks: 'Top Risks', critical_high: 'Critical/High', risks_by_category: 'Risks by Category',
    risks_by_level: 'Risks by Residual Level', ai_assistant: 'AI Assistant', ask_ai: 'Ask AI Assistant',
    profile: 'Profile', language: 'Language', role: 'Role', organization: 'Organization', sector: 'Sector',
    department: 'Department', root_causes: 'Root Causes', mitigation_plans: 'Mitigation Plans', controls: 'Controls',
    export_excel: 'Export Excel', export_pdf: 'Export PDF', notifications: 'Notifications', no_data: 'No data',
    page: 'Page', of: 'of', next: 'Next', previous: 'Previous', all: 'All', identification_date: 'Identified On',
    close: 'Close', change_password: 'Change Password', current_password: 'Current Password',
    new_password: 'New Password', confirm: 'Confirm', add_update: 'Add Update', add_mitigation: 'Add Mitigation Plan',
    suggest_mitigations: 'Suggest Mitigations with AI', loading: 'Loading...', error_occurred: 'An error occurred',
    welcome: 'Welcome', invalid_credentials: 'Invalid credentials', results: 'results'
  }
};

export function t(key, lang) {
  const l = lang || localStorage.getItem('lang') || 'ar';
  return translations[l]?.[key] || translations.ar[key] || key;
}

export function applyDocumentDirection(lang) {
  document.documentElement.lang = lang;
  document.documentElement.dir = lang === 'ar' ? 'rtl' : 'ltr';
}
