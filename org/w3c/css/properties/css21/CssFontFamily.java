// $Id$
// Author: Yves Lafon <ylafon@w3.org>
//
// (c) COPYRIGHT MIT, ERCIM and Keio University, 2012.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.css.properties.css21;

import org.w3c.css.util.ApplContext;
import org.w3c.css.util.InvalidParamException;
import org.w3c.css.values.CssExpression;
import org.w3c.css.values.CssIdent;
import org.w3c.css.values.CssLayerList;
import org.w3c.css.values.CssTypes;
import org.w3c.css.values.CssValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.w3c.css.values.CssOperator.COMMA;
import static org.w3c.css.values.CssOperator.SPACE;

/**
 * @spec http://www.w3.org/TR/2011/REC-CSS2-20110607/fonts.html#font-family-prop
 */
public class CssFontFamily extends org.w3c.css.properties.css.CssFontFamily {

	public static final ArrayList<CssIdent> genericNames;
	public static final ArrayList<CssIdent> reservedNames;

	static {
		String[] _genericNames = {
				"serif", "sans-serif", "cursive",
				"fantasy", "monospace"};

		String[] _reservedNames = {"inherit", "initial", "default"};

		genericNames = new ArrayList<CssIdent>(_genericNames.length);
		for (String s : _genericNames) {
			genericNames.add(CssIdent.getIdent(s));
		}
		reservedNames = new ArrayList<CssIdent>(_reservedNames.length);
		for (String s : _reservedNames) {
			reservedNames.add(CssIdent.getIdent(s));
		}
	}

	static CssIdent getGenericFontName(CssIdent ident) {
		int pos = genericNames.indexOf(ident);
		if (pos >= 0) {
			return genericNames.get(pos);
		}
		return null;
	}

	static CssIdent getReservedFontName(CssIdent ident) {
		int pos = reservedNames.indexOf(ident);
		if (pos >= 0) {
			return reservedNames.get(pos);
		}
		return null;
	}

	private void checkExpression(ApplContext ac, ArrayList<CssValue> curval,
								 ArrayList<CssIdent> values, boolean check, Set<String> distinct) {
		CssIdent val;
		if (values.size() > 1) {
			// create a value out of that. We could even create
			// a CssString for the output (TODO ?)
			StringBuilder sb = new StringBuilder();
			boolean addSpace = false;
			for (CssIdent id : values) {
				if (addSpace) {
					sb.append(' ');
				} else {
					addSpace = true;
				}
				sb.append(id);
			}
			ac.getFrame().addWarning("with-space", 1);
			val = new CssIdent(sb.toString());
		} else {
			val = values.get(0);
			// could be done in the consistency check, but...
			if (null != getGenericFontName(val)) {
				hasGenericFontFamily = true;
			}
			if (inherit.equals(val)) {
				val = inherit;
			}
		}
		// check duplicate font family names
		if (distinct.contains(val.toString())) {
			ac.getFrame().addWarning("no-duplicate-fonts", 2);
		} else {
			distinct.add(val.toString());
		}
		curval.add(val);
	}

	// final consistency check
	private void checkValues(ApplContext ac, ArrayList<CssValue> values)
			throws InvalidParamException {
		// we need to check that we don't have 'inherit' in multiple values
		if (values.size() > 1) {
			for (CssValue val : values) {
				if (inherit.equals(val)) {
					throw new InvalidParamException("unrecognize", ac);
				}
			}
		}
	}

	/**
	 * Creates a new CssFontFamily
	 *
	 * @param expression The expression for this property
	 * @throws org.w3c.css.util.InvalidParamException
	 *          Expressions are incorrect
	 */
	public CssFontFamily(ApplContext ac, CssExpression expression, boolean check)
			throws InvalidParamException {

		ArrayList<CssValue> values = new ArrayList<CssValue>();
		Set<String> distinctFontFamilyNames = new HashSet<String>();

		while (!expression.end()) {
			char op = expression.getOperator();
			CssValue val = expression.getValue();
			switch (val.getType()) {
				case CssTypes.CSS_STRING:
					String s = val.toString();
					// check duplicate font family names
					if (distinctFontFamilyNames.contains(s)) {
						ac.getFrame().addWarning("no-duplicate-fonts", 2);
					} else {
						distinctFontFamilyNames.add(s);
					}

					// check it's not a quoted reserved keyword
					if (s.length() > 2) {
						// we remove quotes and check it's not reserved.
						CssIdent id = new CssIdent(s.substring(1, s.length() - 1));
						if (getGenericFontName(id) != null) {
							ac.getFrame().addWarning("generic-family.quote", 2);
						}
					}
					values.add(val);
					break;
				case CssTypes.CSS_IDENT:
					ArrayList<CssIdent> idval = new ArrayList<CssIdent>();
					idval.add((CssIdent) val);
					// we add idents if separated by spaces...
					while (op == SPACE && expression.getRemainingCount() > 1) {
						expression.next();
						op = expression.getOperator();
						val = expression.getValue();
						if (val.getType() == CssTypes.CSS_IDENT) {
							idval.add((CssIdent) val);
						} else {
							throw new InvalidParamException("value", val,
									getPropertyName(), ac);
						}
					}
					checkExpression(ac, values, idval, check, distinctFontFamilyNames);
					break;
				default:
					throw new InvalidParamException("value", val,
							getPropertyName(), ac);
			}
			expression.next();
			if (!expression.end() && (op != COMMA)) {
				throw new InvalidParamException("operator",
						((new Character(op)).toString()), ac);
			}
		}
		checkValues(ac, values);
		value = (values.size() > 1) ? new CssLayerList(values) : values.get(0);
	}

	public CssFontFamily(ApplContext ac, CssExpression expression)
			throws InvalidParamException {
		this(ac, expression, false);
	}

}

