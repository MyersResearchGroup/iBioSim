const ExcelJS = require('exceljs');
const PDFDocument = require('pdfkit');

async function risksToExcelBuffer(risks) {
  const workbook = new ExcelJS.Workbook();
  const sheet = workbook.addWorksheet('Risk Register');

  sheet.columns = [
    { header: 'Risk ID', key: 'risk_code', width: 16 },
    { header: 'Organization', key: 'organization_name_en', width: 24 },
    { header: 'Category', key: 'category_name_en', width: 20 },
    { header: 'Risk Statement', key: 'risk_statement', width: 60 },
    { header: 'Risk Owner', key: 'risk_owner_name', width: 24 },
    { header: 'Likelihood', key: 'likelihood_score', width: 12 },
    { header: 'Impact', key: 'impact_score', width: 10 },
    { header: 'Inherent Rating', key: 'inherent_risk_rating', width: 16 },
    { header: 'Control Sufficiency', key: 'control_environment_sufficiency', width: 18 },
    { header: 'Residual Level', key: 'residual_risk_level', width: 16 },
    { header: 'Residual Priority', key: 'residual_risk_priority', width: 16 },
    { header: 'Status', key: 'status', width: 14 },
    { header: 'Identification Date', key: 'identification_date', width: 18 }
  ];
  sheet.getRow(1).font = { bold: true };
  sheet.addRows(risks);

  return workbook.xlsx.writeBuffer();
}

function risksToPdfStream(risks, res) {
  const doc = new PDFDocument({ margin: 30, size: 'A4', layout: 'landscape' });
  doc.pipe(res);

  doc.fontSize(16).text('Asir Health Cluster - Risk Register', { align: 'center' });
  doc.moveDown();

  risks.forEach((r, idx) => {
    doc.fontSize(10).text(`${idx + 1}. [${r.risk_code}] ${r.risk_statement}`);
    doc.fontSize(8).fillColor('gray').text(
      `Category: ${r.category_name_en || r.category_name_ar || '-'}  |  Inherent: ${r.inherent_risk_rating || '-'}  |  Residual: ${r.residual_risk_level || '-'}  |  Status: ${r.status}`
    );
    doc.fillColor('black').moveDown(0.5);
  });

  doc.end();
}

module.exports = { risksToExcelBuffer, risksToPdfStream };
