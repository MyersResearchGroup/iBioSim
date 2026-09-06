-- =====================================================================
-- Seed data for Asir Health Cluster ERMS
-- Categories/subcategories transcribed from the source risk register
-- ("سجل مخاطر تجمع عسير الصحي") sheets المراجع / سجل المخاطر.
-- =====================================================================

-- 1. Roles ------------------------------------------------------------
INSERT INTO roles (code, name_en, name_ar, description) VALUES
  ('ADMIN',      'Administrator', 'مدير النظام',        'Full system access, user & configuration management'),
  ('MANAGER',    'Manager',       'مدير إدارة المخاطر',  'Manage risks, controls, approve residual ratings, view all reports'),
  ('RISK_OWNER', 'Risk Owner',    'مالك الخطر',          'Create/update risks and mitigation plans owned by them'),
  ('USER',       'Viewer',        'مستخدم',              'Read-only access to dashboards and risk register');

-- 2. Organizations ------------------------------------------------------
INSERT INTO organizations (code, name_en, name_ar, org_type, parent_id) VALUES
  ('HHC', 'Saudi Health Holding Company', 'شركة الصحة القابضة', 'HOLDING', NULL);
INSERT INTO organizations (code, name_en, name_ar, org_type, parent_id) VALUES
  ('CL(AS)', 'Asir Health Cluster', 'تجمع عسير الصحي', 'CLUSTER',
    (SELECT id FROM organizations WHERE code = 'HHC'));

-- 3. Sectors / Departments (sample from the register) --------------------
INSERT INTO sectors (organization_id, name_en, name_ar)
  SELECT id, 'Executive Affairs for Academic, Training & Research', 'الإدارة التنفيذية للشؤون الأكاديمية والتدريب والبحث'
  FROM organizations WHERE code = 'CL(AS)';

INSERT INTO departments (sector_id, name_en, name_ar)
  SELECT id, 'Cluster Department', 'إدارة في التجمع' FROM sectors LIMIT 1;

-- 4. Risk categories & subcategories (from sheet المراجع) ----------------
INSERT INTO risk_categories (name_ar, name_en, description_ar) VALUES
('الاستراتيجية', 'Strategy', 'المخاطر الناشئة عن عدم قدرة شركة صحة القابضة والتجمعات الصحية على تحقيق غاياتها وتوجهها وأهدافها الاستراتيجية طويلة المدى.'),
('إدارة اصحاب المصلحة', 'Stakeholder Management', 'المخاطر الناشئة عن عدم فعالية الشركة القابضة والتجمعات الصحية في بناء وإدارة والحفاظ على العلاقات مع الأطراف المعنية بقطاع الصحة.'),
('التغيير والثقافة', 'Change & Culture', 'المخاطر الناشئة عن عدم وضوح الأهداف والمبادئ المشتركة والتي تؤثر على بيئة العمل وعزيمة وانتماء الموظفين.'),
('رأس المال البشري', 'Human Capital', 'المخاطر الناشئة عن عدم الفعالية في تخطيط وتحديد الموارد البشرية اللازمة.'),
('السمعة', 'Reputation', 'المخاطر الناشئة عن عدم الفعالية في الحفاظ على صورة ذهنية ملائمة للصحة القابضة.'),
('الحوكمة', 'Governance', 'المخاطر الناشئة عن عدم الفعالية في تحديد الأدوار والمسؤوليات وتنفيذ ممارسات الحوكمة.'),
('المخاطر الناشئة', 'Emerging Risks', 'المخاطر التي تظهر حديثاً في البيئة الخارجية أو تعتبر مألوفة ولكن تحدث في سياق جديد.'),
('السريرية', 'Clinical', 'المخاطر الناشئة عن عدم الفعالية في تقديم الخدمات الصحية داخل المرافق الطبية.'),
('الأمن السيبراني', 'Cybersecurity', 'المخاطر الناشئة عن عدم الفعالية في الحفاظ على سرّية وسلامة وتوافر المعلومات والبيانات.'),
('إدارة البيانات', 'Data Management', 'المخاطر الناشئة عن عدم الفعالية في ضمان جودة عالية للبيانات طوال دورة حياتها.'),
('التقنية', 'Technology', 'المخاطر الناشئة عن عدم الفعالية في الاستخدام الأمثل لموارد تقنية المعلومات.'),
('القانونية والالتزام', 'Legal & Compliance', 'المخاطر الناشئة عن عدم الالتزام بالأنظمة والسياسات والتشريعات والعقود.'),
('المالية', 'Financial', 'المخاطر المرتبطة بالاستدامة المالية والتمويل والموازنة والتكاليف التشغيلية.');

INSERT INTO risk_subcategories (category_id, name_ar, name_en, description_ar) VALUES
((SELECT id FROM risk_categories WHERE name_ar='الاستراتيجية'), 'التخطيط', 'Planning', 'المخاطر المحتملة المرتبطة بتطوير الخطة الاستراتيجية وخططها التشغيلية الداعمة.'),
((SELECT id FROM risk_categories WHERE name_ar='الاستراتيجية'), 'إدارة الأداء', 'Performance Management', 'المخاطر المرتبطة بتحديد مستوى الأداء المطلوب واتخاذ التدابير التصحيحية.'),
((SELECT id FROM risk_categories WHERE name_ar='الاستراتيجية'), 'المشاريع والمبادرات', 'Projects & Initiatives', 'المخاطر المرتبطة بالقدرة على تنفيذ المشاريع والمبادرات بالوقت والجودة المحددة.'),
((SELECT id FROM risk_categories WHERE name_ar='الاستراتيجية'), ' جاهزية التجمعات الصحية', 'Cluster Readiness', 'المخاطر المرتبطة بجاهزية التجمعات الصحية وتحقيق متطلبات التخرج والاستقلالية.'),
((SELECT id FROM risk_categories WHERE name_ar='رأس المال البشري'), 'استقطاب الخبرات والكفاءات', 'Talent Acquisition', 'المخاطر المرتبطة بالتخطيط واستقطاب المهنيين ذوي الكفاءة العالية.'),
((SELECT id FROM risk_categories WHERE name_ar='رأس المال البشري'), 'تأهيل وتطوير وتدريب الكوادر البشرية', 'Training & Development', 'المخاطر المرتبطة بتنفيذ برامج التدريب والتطوير المهني بناءً على الاحتياجات.'),
((SELECT id FROM risk_categories WHERE name_ar='رأس المال البشري'), 'إبقاء وتحفيز المواهب', 'Talent Retention', 'المخاطر المرتبطة بالحفاظ على الموظفين المتميزين وتحفيزهم.'),
((SELECT id FROM risk_categories WHERE name_ar='رأس المال البشري'), 'التعاقب الوظيفي', 'Succession Planning', 'المخاطر المرتبطة بتأهيل الكفاءات المطلوبة لتأمين بدلاء للمناصب القيادية.'),
((SELECT id FROM risk_categories WHERE name_ar='التقنية'), 'الحوسبة السحابية وتشغيل الأنظمة', 'Cloud Computing & Operations', 'مخاطر جاهزية ومرونة البنية التحتية التقنية للحوسبة السحابية.'),
((SELECT id FROM risk_categories WHERE name_ar='التقنية'), 'التحول الرقمي', 'Digital Transformation', 'مخاطر التحول من النظم التقليدية الى الأنظمة الرقمية.'),
((SELECT id FROM risk_categories WHERE name_ar='المالية'), 'الميزانية', 'Budget', 'مخاطر التمويل والموازنة والدعم المالي للبرامج والمبادرات.'),
((SELECT id FROM risk_categories WHERE name_ar='السريرية'), ' سلامة المرضى', 'Patient Safety', 'مخاطر الحفاظ على سلامة المرضى وتقليل الأضرار غير الضرورية.'),
((SELECT id FROM risk_categories WHERE name_ar='الأمن السيبراني'), 'سرّية الأصول المعلوماتية', 'Information Confidentiality', 'مخاطر حماية الأصول المعلوماتية من الوصول غير المصرح به.');

-- 5. Impact areas (24 columns from سجل المخاطر header) ---------------------
INSERT INTO impact_areas (name_ar, name_en, display_order) VALUES
('استمرارية الأعمال', 'Business Continuity', 1),
('التحول الرقمي', 'Digital Transformation', 2),
('الأمن السيبراني', 'Cybersecurity', 3),
('تنفيذ المشاريع والمبادرات', 'Project & Initiative Execution', 4),
('الأمن والسلامة', 'Security & Safety', 5),
('جاهزية التجمعات الصحية للتخرج', 'Cluster Graduation Readiness', 6),
('الالتزام بالأنظمة والتشريعات', 'Regulatory Compliance', 7),
('الحوكمة', 'Governance', 8),
('تفاعلات الرأي العام', 'Public Opinion', 9),
('أصحاب المصلحة الخارجيين', 'External Stakeholders', 10),
('الموظفين', 'Employees', 11),
('الاستدامة المالية', 'Financial Sustainability', 12),
('التمويل والموازنة', 'Funding & Budget', 13),
('تكلفة المستفيد', 'Beneficiary Cost', 14),
('الخسائر التشغيلية', 'Operational Losses', 15),
('توفر الخدمات الصحية', 'Healthcare Services Availability', 16),
('جودة الرعاية الصحية', 'Healthcare Quality', 17),
('سلاسل التوريد', 'Supply Chains', 18),
('الأحداث الجسيمة', 'Major Incidents', 19),
('تجربة المستفيد', 'Beneficiary Experience', 20),
('الأمراض غير المعدية', 'Non-Communicable Diseases', 21),
('جذب القوى العاملة', 'Workforce Attraction', 22),
('الحفاظ على القوى العاملة', 'Workforce Retention', 23),
('توطين المهن (التمريض)', 'Nursing Localization', 24);

-- 6. Users (password for ALL seed users = "ChangeMe123!" — bcrypt hash below) --
-- Hash generated with bcrypt, cost 12. CHANGE IMMEDIATELY IN PRODUCTION.
INSERT INTO users (full_name, email, password_hash, job_title, role_id, organization_id, department_id, preferred_lang)
VALUES
('System Administrator', 'admin@asirhealth.sa',
 '$2b$12$a/jGm/NuLXKr5jIPf9iaXOuRcEykEYepkXQhnlVpsYiyhwGs6QFbK',
 'ERM Administrator', (SELECT id FROM roles WHERE code='ADMIN'),
 (SELECT id FROM organizations WHERE code='CL(AS)'), NULL, 'ar'),
('د. موسى بحاري', 'erm.manager@asirhealth.sa',
 '$2b$12$a/jGm/NuLXKr5jIPf9iaXOuRcEykEYepkXQhnlVpsYiyhwGs6QFbK',
 'إدارة المخاطر المؤسسية', (SELECT id FROM roles WHERE code='MANAGER'),
 (SELECT id FROM organizations WHERE code='CL(AS)'), NULL, 'ar'),
('د.عبدالله سعيد', 'risk.owner@asirhealth.sa',
 '$2b$12$a/jGm/NuLXKr5jIPf9iaXOuRcEykEYepkXQhnlVpsYiyhwGs6QFbK',
 'نائب الرئيس التنفيذي للشؤون الأكاديمية والتدريب والبحث', (SELECT id FROM roles WHERE code='RISK_OWNER'),
 (SELECT id FROM organizations WHERE code='CL(AS)'),
 (SELECT id FROM departments LIMIT 1), 'ar');

-- 7. Sample risks (transcribed from the register, first 3 rows) -----------
INSERT INTO risks (risk_code, organization_id, sector_id, department_id, risk_owner_id,
                    identification_date, risk_statement, root_causes, category_id, subcategory_id,
                    likelihood_score, impact_score, inherent_risk_rating,
                    control_environment_sufficiency, residual_risk_priority, residual_risk_level, status)
VALUES
('CL(AS).R-1',
 (SELECT id FROM organizations WHERE code='CL(AS)'),
 (SELECT id FROM sectors LIMIT 1),
 (SELECT id FROM departments LIMIT 1),
 (SELECT id FROM users WHERE email='risk.owner@asirhealth.sa'),
 '2026-03-30',
 'احتمالية إلغاء مؤتمرات وندوات بسبب عدم وجود تمويل قد يؤدي إلى فقدان فرص تطوير مهارات الموظفين',
 'تأخر الدعم المالي',
 (SELECT id FROM risk_categories WHERE name_ar='المالية'),
 (SELECT id FROM risk_subcategories WHERE name_ar='الميزانية'),
 4, 2, 'متوسط', 'ضعيف', 'أولوية 10', 'مستوى 3', 'OPEN'),

('CL(AS).R-2',
 (SELECT id FROM organizations WHERE code='CL(AS)'),
 (SELECT id FROM sectors LIMIT 1),
 (SELECT id FROM departments LIMIT 1),
 (SELECT id FROM users WHERE email='risk.owner@asirhealth.sa'),
 '2026-03-30',
 'احتمالية عدم تجديد الاعتماد الأكاديمي لبعض برامج الدراسات العليا بسبب نقص المعايير الأكاديمية والسريرية',
 'نقص المعايير الأكاديمية والسريرية',
 (SELECT id FROM risk_categories WHERE name_ar='رأس المال البشري'),
 (SELECT id FROM risk_subcategories WHERE name_ar='تأهيل وتطوير وتدريب الكوادر البشرية'),
 4, 2, 'متوسط', 'ضعيف', 'أولوية 10', 'مستوى 3', 'OPEN'),

('CL(AS).R-7',
 (SELECT id FROM organizations WHERE code='CL(AS)'),
 (SELECT id FROM sectors LIMIT 1),
 (SELECT id FROM departments LIMIT 1),
 (SELECT id FROM users WHERE email='risk.owner@asirhealth.sa'),
 '2026-03-30',
 'احتمالية عدم التمكن من إطلاق منصة التدريب الرقمية بسبب تأخر الدعم المالي',
 'نقص الدعم المالي',
 (SELECT id FROM risk_categories WHERE name_ar='المالية'),
 (SELECT id FROM risk_subcategories WHERE name_ar='الميزانية'),
 5, 4, 'عالي', 'ضعيف', 'أولوية 5', 'مستوى 2', 'MITIGATING');

INSERT INTO risk_impact_scores (risk_id, impact_area_id, score)
SELECT (SELECT id FROM risks WHERE risk_code='CL(AS).R-1'), id, 2 FROM impact_areas WHERE name_ar IN ('الموظفين','التمويل والموازنة');
INSERT INTO risk_impact_scores (risk_id, impact_area_id, score)
SELECT (SELECT id FROM risks WHERE risk_code='CL(AS).R-7'), id, 4 FROM impact_areas WHERE name_ar IN ('التحول الرقمي','تنفيذ المشاريع والمبادرات','جاهزية التجمعات الصحية للتخرج');

INSERT INTO controls (control_code, risk_id, description, control_type,
                       is_activated_approved, is_shared_implemented, is_fully_committed, is_effective)
VALUES
('CTR-CL(AS).R-1', (SELECT id FROM risks WHERE risk_code='CL(AS).R-1'), 'لا يوجد', 'NONE', false, false, false, false),
('CTR-CL(AS).R-7', (SELECT id FROM risks WHERE risk_code='CL(AS).R-7'), 'تمت مخاطبة إدارة الميزانية', 'CORRECTIVE', false, false, false, false);
